package tachiyomi.cast

import android.content.Context
import androidx.core.net.toUri
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouterParams
import animiru.domain.player.interactor.TrackSelect
import com.google.android.gms.cast.MediaError
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import eu.kanade.tachiyomi.animesource.model.Video
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
import tachiyomi.cast.domain.CodecInformation
import tachiyomi.cast.domain.TrackInformation
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.anime.model.Anime

// Some code taken from https://github.com/MakD/AFinity/blob/master/app/src/main/java/com/makd/afinity/cast/CastManager.kt
class CastManagerImpl(
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
        videoInformation: CodecInformation,
        subtitleTracks: List<TrackInformation>,
        audioTracks: List<TrackInformation>,
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

            _castState.update { it.copy(isLoading = true) }

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
                setCurrentTime(startPosition * 1000L)
                setActiveTrackIds(preferred)
            }.build()

            val loadTask = client.load(loadRequest)
            loadTask.setResultCallback { result ->
                if (result.status.isSuccess) {
                    _castState.update { it.copy(isLoading = false) }
                    if (startPosition > 0L) {
                        client.seek(
                            MediaSeekOptions.Builder()
                                .setPosition(startPosition * 1000L)
                                .build(),
                        )
                        scope.launch {
                            _castEvent.emit(CastEvent.OnSecondReached(startPosition.toInt()))
                        }
                    }
                }
            }

            _castState.update {
                it.copy(
                    hasLoadedVideo = true,
                )
            }

            _castEvent.emit(CastEvent.Ready)
        }
    }

    override fun stopRemoteMediaClient() {
        remoteMediaClient?.stop()
    }

    override fun handleCastManagerEvent(event: CastManagerEvent) {
        when (event) {
            is CastManagerEvent.Next -> {
                scope.launch {
                    _castEvent.emit(CastEvent.NextEpisode(event.next))
                }
            }
            CastManagerEvent.PlayPause -> {
                if (castState.value.playing) {
                    remoteMediaClient?.pause()
                } else {
                    remoteMediaClient?.play()
                }
            }
        }
    }

    private fun updatePositionFromRemote() {
        try {
            val client = remoteMediaClient ?: return
            val mediaStatus = client.mediaStatus
            if (mediaStatus != null) {
                _castState.update {
                    it.copy(
                        playing = mediaStatus.playerState == MediaStatus.PLAYER_STATE_PLAYING,
                        buffering =
                        mediaStatus.playerState == MediaStatus.PLAYER_STATE_BUFFERING ||
                            mediaStatus.playerState == MediaStatus.PLAYER_STATE_LOADING,
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
            _castState.update { _ -> CastState() }
            scope.launch {
                _castEvent.emit(CastEvent.ConnectionStart)
            }
        }

        override fun onSessionStarted(session: CastSession, sessionId: String) {
            val deviceName = session.castDevice?.friendlyName
            castSession = session
            remoteMediaClient = session.remoteMediaClient
            remoteMediaClient?.registerCallback(remoteMediaClientCallback)
            remoteMediaClient?.addProgressListener(remoteMediaClientProgressListener, 1000)

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
                _castEvent.emit(CastEvent.ConnectionError)
            }
        }

        override fun onSessionEnding(session: CastSession) {
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            val state = castState.value
            remoteMediaClient?.unregisterCallback(remoteMediaClientCallback)
            remoteMediaClient?.removeProgressListener(remoteMediaClientProgressListener)
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
            remoteMediaClient?.addProgressListener(remoteMediaClientProgressListener, 1000)
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

    private val remoteMediaClientProgressListener = RemoteMediaClient.ProgressListener { progressMs, _ ->
        _castState.update {
            it.copy(
                position = progressMs / 1000L,
            )
        }
        if (castState.value.playing) {
            scope.launch {
                _castEvent.emit(CastEvent.OnSecondReached(position = progressMs.div(1000).toInt()))
            }
        }
    }

    private val remoteMediaClientCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            updatePositionFromRemote()
        }

        override fun onMediaError(error: MediaError) {
            // TODO(cast): Error handling
        }
    }
}
