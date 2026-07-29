package eu.kanade.tachiyomi.ui.player.mpv

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.view.KeyCharacterMap
import android.view.KeyEvent
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import animiru.feature.mpvfiles.MpvConfig
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.network.NetworkPreferences
import eu.kanade.tachiyomi.ui.player.Debanding
import eu.kanade.tachiyomi.ui.player.VideoFilters
import eu.kanade.tachiyomi.ui.player.controls.components.panels.toColorHexString
import eu.kanade.tachiyomi.ui.player.settings.AdvancedPlayerPreferences
import eu.kanade.tachiyomi.ui.player.settings.AudioPreferences
import eu.kanade.tachiyomi.ui.player.settings.DecoderPreferences
import eu.kanade.tachiyomi.ui.player.settings.PlayerPreferences
import eu.kanade.tachiyomi.ui.player.settings.SubtitleAssOverride
import eu.kanade.tachiyomi.ui.player.settings.SubtitlePreferences
import `is`.xyz.mpv.KeyMapping
import `is`.xyz.mpv.MPV
import `is`.xyz.mpv.MPVNode
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import logcat.LogPriority
import logcat.logcat
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.collections.component1
import kotlin.collections.component2

class MPVPlayer(
    context: Context,
    videoOutput: String,
    playerPreferences: PlayerPreferences = Injekt.get(),
    decoderPreferences: DecoderPreferences = Injekt.get(),
    networkPreferences: NetworkPreferences = Injekt.get(),
    advancedPreferences: AdvancedPlayerPreferences = Injekt.get(),
    private val subtitlePreferences: SubtitlePreferences = Injekt.get(),
    private val audioPreferences: AudioPreferences = Injekt.get(),
) : MPV.EventObserver, MPV.LogObserver, AudioManager.OnAudioFocusChangeListener {

    val mpv: MPV
    private val handler = Handler(context.mainLooper)

    private val audioManager by lazy { context.getSystemService(AUDIO_SERVICE) as AudioManager }
    private var restoreAudioFocus: () -> Unit = {}
    private var audioFocusRequest: AudioFocusRequestCompat? = null

    @Volatile
    var isExiting = false
    private var httpError: String? = null

    private val _eventFlow = MutableSharedFlow<Event>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        val cachePath: String = context.cacheDir.path

        val mpvDir = UniFile.fromFile(context.filesDir)!!.createDirectory(MPV_DIR)!!

        val mpvConfFile = mpvDir.createFile("mpv.conf")!!
        advancedPreferences.mpvConf.get().let { mpvConfFile.writeText(it) }
        val mpvInputFile = mpvDir.createFile("input.conf")!!
        advancedPreferences.mpvInput.get().let { mpvInputFile.writeText(it) }

        mpv = MPV(context) {
            it.setOptionString("config", "yes")
            it.setOptionString("config-dir", context.filesDir.resolve(MPV_DIR).toString())
            it.setOptionString("gpu-shader-cache-dir", cachePath)
            it.setOptionString("icc-cache-dir", cachePath)
            it.setOptionString("keep-open", "yes")
        }

        val optionNameRegex = Regex("""^(?:--)?([\w-]+)(?:=|$)""", RegexOption.MULTILINE)
        val mpvOptionNames = optionNameRegex.findAll(advancedPreferences.mpvConf.get()).map {
            it.groupValues[1].removePrefix("no-")
        }.toSet()

        // Set mpv option unless it's present in mpv.conf
        fun setSafeOptionString(name: String, value: String) {
            if (name in mpvOptionNames) return
            mpv.setOptionString(name, value)
        }

        mpv.setOptionString("vo", videoOutput)
        setSafeOptionString("profile", "fast")
        mpv.setOptionString("hwdec", if (decoderPreferences.tryHWDecoding.get()) "mediacodec,mediacodec-copy" else "no")
        if (decoderPreferences.useYUV420P.get()) {
            mpv.setOptionString("vf", "format=yuv420p")
        }

        mpv.setOptionString("msg-level", "all=" + if (networkPreferences.verboseLogging.get()) "v" else "warn")
        mpv.setPropertyBoolean("input-default-bindings", true)
        mpv.setOptionString("idle", "yes")
        mpv.setOptionString("ytdl", "no")
        setSafeOptionString("tls-verify", "yes")
        setSafeOptionString("tls-ca-file", "${context.filesDir.path}/${MpvConfig.MPV_DIR}/cacert.pem")

        // Selection is handled in viewmodel
        mpv.setOptionString("sid", "no")
        mpv.setOptionString("aid", "no")

        // Limit demuxer cache since the defaults are too high for mobile devices
        val cacheMegs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) 64 else 32
        setSafeOptionString("demuxer-max-bytes", "${cacheMegs * 1024 * 1024}")
        setSafeOptionString("demuxer-max-back-bytes", "${cacheMegs * 1024 * 1024}")

        val screenshotDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).also {
            it.mkdirs()
        }
        mpv.setOptionString("screenshot-directory", screenshotDir.path)

        VideoFilters.entries.forEach {
            mpv.setOptionString(it.mpvProperty, it.preference(decoderPreferences).get().toString())
        }

        mpv.setOptionString("speed", playerPreferences.playerSpeed.get().toString())
        // workaround for <https://github.com/mpv-player/mpv/issues/14651>
        setSafeOptionString("vd-lavc-film-grain", "cpu")

        when (decoderPreferences.debanding.get()) {
            Debanding.None -> {}
            Debanding.CPU -> mpv.setOptionString("vf", "gradfun=radius=12")
            Debanding.GPU -> mpv.setOptionString("deband", "yes")
        }

        advancedPreferences.playerStatisticsPage.get().let {
            if (it != 0) {
                mpv.command("script-binding", "stats/display-stats-toggle")
                mpv.command("script-binding", "stats/display-page-$it")
            }
        }

        mpv.addObserver(this)
        mpv.addLogObserver(this)

        setupSubtitlesOptions()
        setupAudio()

        mapOf(
            "eof-reached" to MPV.mpvFormat.MPV_FORMAT_FLAG,

            "user-data/aniyomi/show_text" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/toggle_ui" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/show_panel" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/software_keyboard" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/set_button_title" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/reset_button_title" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/toggle_button" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/switch_episode" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/pause" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/seek_by" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/seek_to" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/seek_by_with_text" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/seek_to_with_text" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/launch_int_picker" to MPV.mpvFormat.MPV_FORMAT_STRING,
            "user-data/aniyomi/show_seek_text" to MPV.mpvFormat.MPV_FORMAT_STRING,
        ).forEach { (name, format) ->
            mpv.observeProperty(name, format)
        }
    }

    private fun UniFile.writeText(text: String) {
        this.openOutputStream().use {
            it.write(text.toByteArray())
        }
    }

    private fun setupAudio() {
        mpv.setOptionString("alang", audioPreferences.preferredAudioLanguages.get())
        mpv.setOptionString("audio-delay", (audioPreferences.audioDelay.get() / 1000.0).toString())
        mpv.setOptionString("audio-pitch-correction", audioPreferences.enablePitchCorrection.get().toString())
        mpv.setOptionString("volume-max", (audioPreferences.volumeBoostCap.get() + 100).toString())

        audioPreferences.audioChannels.get().let {
            mpv.setPropertyString(it.property, it.value)
        }

        val request = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN).also {
            it.setAudioAttributes(
                AudioAttributesCompat.Builder().setUsage(AudioAttributesCompat.USAGE_MEDIA)
                    .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC).build(),
            )
            it.setOnAudioFocusChangeListener(this)
        }.build()
        AudioManagerCompat.requestAudioFocus(audioManager, request).let {
            if (it == AudioManager.AUDIOFOCUS_REQUEST_FAILED) return@let
            audioFocusRequest = request
        }
    }

    private fun setupSubtitlesOptions() {
        mpv.setOptionString("sub-delay", (subtitlePreferences.subtitlesDelay.get() / 1000.0).toString())
        mpv.setOptionString("sub-speed", subtitlePreferences.subtitlesSpeed.get().toString())
        mpv.setOptionString(
            "secondary-sub-delay",
            (subtitlePreferences.subtitlesSecondaryDelay.get() / 1000.0).toString(),
        )

        mpv.setOptionString("sub-font", subtitlePreferences.subtitleFont.get())
        subtitlePreferences.overrideSubsASS.get().let {
            mpv.setOptionString("sub-ass-override", it.value)
            if (it != SubtitleAssOverride.No) {
                mpv.setOptionString("sub-ass-justify", "yes")
            }
        }
        mpv.setOptionString("sub-font-size", subtitlePreferences.subtitleFontSize.get().toString())
        mpv.setOptionString("sub-bold", if (subtitlePreferences.boldSubtitles.get()) "yes" else "no")
        mpv.setOptionString("sub-italic", if (subtitlePreferences.italicSubtitles.get()) "yes" else "no")
        mpv.setOptionString("sub-justify", subtitlePreferences.subtitleJustification.get().value)
        mpv.setOptionString("sub-color", subtitlePreferences.textColorSubtitles.get().toColorHexString())
        mpv.setOptionString(
            "sub-back-color",
            subtitlePreferences.backgroundColorSubtitles.get().toColorHexString(),
        )
        mpv.setOptionString("sub-outline-color", subtitlePreferences.borderColorSubtitles.get().toColorHexString())
        mpv.setOptionString("sub-outline-size", subtitlePreferences.subtitleBorderSize.get().toString())
        mpv.setOptionString("sub-border-style", subtitlePreferences.borderStyleSubtitles.get().value)
        mpv.setOptionString("sub-shadow-offset", subtitlePreferences.shadowOffsetSubtitles.get().toString())
        mpv.setOptionString("sub-pos", subtitlePreferences.subtitlePos.get().toString())
        mpv.setOptionString("sub-scale", subtitlePreferences.subtitleFontScale.get().toString())

        val showBlackBars = if (subtitlePreferences.subtitleBlackBars.get()) "yes" else "no"
        mpv.setOptionString("sub-ass-force-margins", showBlackBars)
        mpv.setOptionString("sub-use-margins", showBlackBars)
    }

    override fun eventProperty(property: String) {
        handler.post {
            if (isExiting) return@post
        }
    }

    override fun eventProperty(property: String, value: Long) {
        handler.post {
            if (isExiting) return@post
        }
    }

    override fun eventProperty(property: String, value: Boolean) {
        handler.post {
            if (isExiting) return@post
            when (property) {
                "eof-reached" -> _eventFlow.tryEmit(Event.EOF(value))
            }
        }
    }

    override fun eventProperty(property: String, value: String) {
        handler.post {
            if (isExiting) return@post
            when (property.substringBeforeLast("/")) {
                "user-data/aniyomi" -> _eventFlow.tryEmit(Event.LuaEvent(property, value))
            }
        }
    }

    override fun eventProperty(property: String, value: Double) {
        handler.post {
            if (isExiting) return@post
        }
    }

    override fun eventProperty(property: String, value: MPVNode) {
        handler.post {
            if (isExiting) return@post
        }
    }

    override fun event(eventId: Int, data: MPVNode) {
        handler.post {
            if (isExiting) return@post
            when (eventId) {
                MPV.mpvEvent.MPV_EVENT_FILE_LOADED -> _eventFlow.tryEmit(Event.FileLoaded)
                MPV.mpvEvent.MPV_EVENT_PLAYBACK_RESTART -> isExiting = false
                MPV.mpvEvent.MPV_EVENT_END_FILE -> _eventFlow.tryEmit(Event.EndFile(data))
            }
        }
    }

    override fun logMessage(prefix: String, level: Int, text: String) {
        if (level == MPV.mpvLogLevel.MPV_LOG_LEVEL_ERROR) {
            if (text.startsWith(TRACK_LOAD_FAILURE)) {
                val url = text.removePrefix(TRACK_LOAD_FAILURE).substringBeforeLast(".")
                _eventFlow.tryEmit(Event.TrackLoadFailure(url))
            }
        }

        val logPriority = when (level) {
            MPV.mpvLogLevel.MPV_LOG_LEVEL_FATAL, MPV.mpvLogLevel.MPV_LOG_LEVEL_ERROR -> LogPriority.ERROR
            MPV.mpvLogLevel.MPV_LOG_LEVEL_WARN -> LogPriority.WARN
            MPV.mpvLogLevel.MPV_LOG_LEVEL_INFO -> LogPriority.INFO
            else -> LogPriority.VERBOSE
        }
        if (text.contains("HTTP error")) httpError = text.removePrefix("http: ")
        logcat("$TAG/$prefix", logPriority) { text }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                val oldRestore = restoreAudioFocus
                val wasPlayerPaused = mpv.getPropertyBoolean("pause") ?: true
                mpv.setPropertyBoolean("pause", true)
                restoreAudioFocus = {
                    oldRestore()
                    if (!wasPlayerPaused) mpv.setPropertyBoolean("pause", false)
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mpv.command("multiply", "volume", "0.5")
                restoreAudioFocus = {
                    mpv.command("multiply", "volume", "2")
                }
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                restoreAudioFocus()
                restoreAudioFocus = {}
            }

            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                logcat(TAG, LogPriority.DEBUG) { "didn't get audio focus" }
            }
        }
    }

    fun onKey(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_MULTIPLE || KeyEvent.isModifierKey(event.keyCode)) {
            return false
        }

        var mapped = KeyMapping[event.keyCode]
        if (mapped == null) {
            // Fallback to produced glyph
            if (!event.isPrintingKey) {
                if (event.repeatCount == 0) {
                    logcat(TAG, LogPriority.DEBUG) { "Unmapped non-printable key ${event.keyCode}" }
                }
                return false
            }

            val ch = event.unicodeChar
            if (ch.and(KeyCharacterMap.COMBINING_ACCENT) != 0) {
                return false // dead key
            }
            mapped = ch.toChar().toString()
        }

        if (event.repeatCount > 0) {
            return true // eat event but ignore it, mpv has its own key repeat
        }

        val mod: MutableList<String> = mutableListOf()
        event.isShiftPressed && mod.add("shift")
        event.isCtrlPressed && mod.add("ctrl")
        event.isAltPressed && mod.add("alt")
        event.isMetaPressed && mod.add("meta")

        val action = if (event.action == KeyEvent.ACTION_DOWN) "keydown" else "keyup"
        mod.add(mapped)
        mpv.command(action, mod.joinToString("+"))

        return true
    }

    // ===== End events =====

    fun getHttpError(): String? {
        return httpError
    }

    fun resetHttpError() {
        httpError = null
    }

    fun release() {
        if (isExiting) return
        isExiting = true

        audioFocusRequest?.let {
            AudioManagerCompat.abandonAudioFocusRequest(audioManager, it)
        }
        audioFocusRequest = null

        handler.removeCallbacksAndMessages(null)
        mpv.removeObserver(this)
        mpv.removeLogObserver(this)
        mpv.close()
    }

    sealed interface Event {
        data object FileLoaded : Event
        data class EOF(val value: Boolean) : Event
        data class TrackLoadFailure(val url: String) : Event
        data class EndFile(val node: MPVNode) : Event
        data class LuaEvent(val property: String, val value: String) : Event
    }

    companion object {
        private const val TAG = "mpv"
        private const val MPV_DIR = "mpv"
        const val TRACK_LOAD_FAILURE = "Can not open external file "
    }
}
