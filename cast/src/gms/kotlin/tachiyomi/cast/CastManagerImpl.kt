package tachiyomi.cast

import android.content.Context
import androidx.core.net.toUri
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouterParams
import animiru.domain.player.interactor.TrackSelect
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import dev.vivvvek.seeker.Segment
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.LogPriority
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import tachiyomi.cast.domain.TrackInformation
import tachiyomi.cast.domain.VideoInformation
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.anime.model.Anime

// Some code taken from https://github.com/MakD/AFinity/blob/master/app/src/main/java/com/makd/afinity/cast/CastManager.kt
class CastManagerImpl(
    private val videoInformation: VideoInformation,
    private val trackSelect: TrackSelect,
) : CastManager {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    lateinit var castContext: CastContext
    private var castSession: CastSession? = null
    private var remoteMediaClient: RemoteMediaClient? = null

    @Volatile
    private var initialized = false

    private val _castState = MutableStateFlow(CastState())
    override val castState = _castState.asStateFlow()

    private val _castEvent = MutableSharedFlow<CastEvent>()
    override val castEvent = _castEvent.asSharedFlow()

    override fun initialize(context: Context) {
        if (initialized) return

        try {
            castContext = CastContext.getSharedInstance(context)
            castContext
                .sessionManager
                .addSessionManagerListener(castSessionManagerListener, CastSession::class.java)

            MediaRouter.getInstance(context).routerParams = MediaRouterParams.Builder()
                .setDialogType(MediaRouterParams.DIALOG_TYPE_DYNAMIC_GROUP)
                .setOutputSwitcherEnabled(true)
                .build()

            initialized = true
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to initialize cast" }
            initialized = false
        }
    }

    override fun disconnect() {
        scope.launch {
            try {
                castContext.sessionManager.endCurrentSession(true)
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e) { "Failed to disconnect cast" }
            }
        }
    }

    override fun startCasting(
        video: Video,
        source: AnimeSource,
        anime: Anime,
        episodeTitle: String,
        startPosition: Long,
    ) {
        scope.launch {
            val client = remoteMediaClient
            if (client == null) {
                _castEvent.emit(CastEvent.PlaybackError(CastNotConnectedException()))
                return@launch
            }

            val headers = video.headers
                ?: (source as? AnimeHttpSource)?.headers
                ?: Headers.EMPTY

            val videoInformation = videoInformation.getVideoInformation(
                videoUrl = video.videoUrl,
                headers = headers,
            )

            val maxIndex = videoInformation.tracks.maxByOrNull { it.index }?.index ?: 0
            val externalSubtitleTracks = video.subtitleTracks.mapIndexed { index, track ->
                TrackInformation(
                    index = maxIndex + index,
                    type = "subtitle",
                    contentType = getSubtitleContentType(track.url),
                    title = track.lang,
                    language = "und",
                    contentId = track.url,
                )
            }
            val externalAudioTracks = video.audioTracks.mapIndexed { index, track ->
                TrackInformation(
                    index = maxIndex + externalSubtitleTracks.size + index,
                    type = "audio",
                    contentType = getAudioContentType(track.url),
                    title = track.lang,
                    language = "und",
                    contentId = track.url,
                )
            }
            val subtitleTracks = videoInformation.tracks.filter { it.type == "subtitle" } + externalSubtitleTracks
            val audioTracks = videoInformation.tracks.filter { it.type == "audio" } + externalAudioTracks

            val subtitleMediaTracks = subtitleTracks.map {
                MediaTrack.Builder(it.index, MediaTrack.TYPE_TEXT).apply {
                    setName(it.title)
                    setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    setContentType(it.contentType)
                    setLanguage(it.language)
                    it.contentId?.let { id ->
                        setContentId(id)
                    }
                }.build()
            }

            val audioMediaTracks = audioTracks.map {
                MediaTrack.Builder(it.index, MediaTrack.TYPE_AUDIO).apply {
                    setName(it.title)
                    setSubtype(MediaTrack.SUBTYPE_NONE)
                    setContentType(it.contentType)
                    setLanguage(it.language)
                    it.contentId?.let { id ->
                        setContentId(id)
                    }
                }.build()
            }

            val preferredSubtitle = trackSelect.getPreferredTrackIndex(subtitleTracks, subtitle = true)
            val preferredAudio = trackSelect.getPreferredTrackIndex(audioTracks, subtitle = false)
            val preferred = listOfNotNull(preferredSubtitle?.index, preferredAudio?.index).toLongArray()

            val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
                putString(MediaMetadata.KEY_TITLE, anime.title)
                putString(MediaMetadata.KEY_SUBTITLE, episodeTitle)
                anime.thumbnailUrl?.let {
                    addImage(WebImage(it.toUri()))
                }
            }

            val mediaInfo = MediaInfo.Builder(video.videoUrl).apply {
                setContentType(videoInformation.contentType)
                setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                setMediaTracks(subtitleMediaTracks + audioMediaTracks)
                videoInformation.duration?.let {
                    setStreamDuration(it.times(1000).toLong())
                }
                setMetadata(metadata)
            }.build()

            val loadRequest = MediaLoadRequestData.Builder().apply {
                setMediaInfo(mediaInfo)
                setAutoplay(true)
                setCurrentTime(startPosition)
                setActiveTrackIds(preferred)
            }.build()

            client.load(loadRequest)

            val chapters = videoInformation.chapters.map {
                Segment(
                    name = it.name,
                    start = it.startTime.toFloat(),
                )
            }

            _castState.update {
                it.copy(
                    hasLoadedVideo = true,
                    subTracks = subtitleTracks,
                    audioTracks = audioTracks,
                    chapters = chapters,
                )
            }
        }
    }

    private fun getSubtitleContentType(url: String): String {
        val extension = url.toHttpUrl().pathSegments.last()
            .substringAfterLast(".", "")
            .lowercase()
        return when (extension) {
            "vtt" -> "text/vtt"
            "srt" -> "application/x-subrip"
            "ttml", "dfxp", "xml" -> "application/ttml+xml"
            "smi", "sami" -> "application/smil+xml"
            "ssa", "ass" -> "text/x-ssa"
            else -> "text/vtt"
        }
    }

    private fun getAudioContentType(url: String): String {
        val extension = url.toHttpUrl().pathSegments.last()
            .substringAfterLast(".", "")
            .lowercase()
        return when (extension) {
            "aac" -> "audio/aac"
            "mp3" -> "audio/mpeg"
            "m4a", "m4b" -> "audio/mp4"
            "wav" -> "audio/wav"
            "ogg", "oga", "opus" -> "audio/ogg"
            "flac" -> "audio/flac"
            "webm" -> "audio/webm"
            else -> "audio/mp4"
        }
    }

    private fun updatePositionFromRemote() {
        try {
            val client = remoteMediaClient ?: return
            val position = client.approximateStreamPosition / 1000L
            if (position >= 0) {
                _castState.update { it.copy(position = position) }
            }

            val mediaStatus = client.mediaStatus
            if (mediaStatus != null) {
                val duration = mediaStatus.mediaInfo?.streamDuration?.div(1000L) ?: 0

                _castState.update {
                    it.copy(
                        playing = mediaStatus.playerState == MediaStatus.PLAYER_STATE_PLAYING,
                        loading = mediaStatus.playerState == MediaStatus.PLAYER_STATE_BUFFERING,
                        duration = if (duration > 0) duration else it.duration,
                    )
                }
            }

            castSession?.let { cSession ->
                _castState.update {
                    it.copy(
                        volume = cSession.volume,
                        muted = cSession.isMute,
                    )
                }
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to update cast from remote" }
        }
    }

    private val castSessionManagerListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarting(session: CastSession) {
        }

        override fun onSessionStarted(session: CastSession, sessionId: String) {
            val deviceName = session.castDevice?.friendlyName
            castSession = session
            remoteMediaClient = session.remoteMediaClient
            remoteMediaClient?.registerCallback(remoteMediaClientCallback)

            _castState.update {
                it.copy(
                    isConnected = true,
                    deviceName = deviceName,
                    volume = session.volume,
                    muted = session.isMute,
                )
            }

            scope.launch {
                _castEvent.emit(CastEvent.Connected)
            }
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            logcat(LogPriority.ERROR) { "Cast session failed: $error" }
            scope.launch {
                _castEvent.emit(CastEvent.PlaybackError(CastStartException(error)))
            }
        }

        override fun onSessionEnding(session: CastSession) {
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            val state = castState.value
            remoteMediaClient?.unregisterCallback(remoteMediaClientCallback)
            remoteMediaClient = null
            castSession = null

            scope.launch {
                _castEvent.emit(
                    CastEvent.Disconnected(state.position),
                )
            }
        }

        override fun onSessionResuming(session: CastSession, sessionId: String) {
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            castSession = session
            remoteMediaClient = session.remoteMediaClient
            remoteMediaClient?.registerCallback(remoteMediaClientCallback)

            _castState.update {
                it.copy(
                    isConnected = true,
                    deviceName = session.castDevice?.friendlyName,
                )
            }
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {
        }
    }

    private val remoteMediaClientCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            updatePositionFromRemote()
        }
    }
}
