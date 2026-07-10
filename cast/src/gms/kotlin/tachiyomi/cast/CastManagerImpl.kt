package tachiyomi.cast

import android.content.Context
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouterParams
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
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
import tachiyomi.core.common.util.system.logcat

// Some code taken from https://github.com/MakD/AFinity/blob/master/app/src/main/java/com/makd/afinity/cast/CastManager.kt
class CastManagerImpl : CastManager {

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

    override fun startCasting(video: Video, startPosition: Long) {
        scope.launch {
            val client = remoteMediaClient
            if (client == null) {
                _castEvent.emit(CastEvent.PlaybackError(CastNotConnectedException()))
                return@launch
            }

            val media = MediaInfo.Builder(video.videoUrl)
                .setContentType("video/x-matroska")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .build()

            val loadRequest = MediaLoadRequestData.Builder()
                .setMediaInfo(media)
                .setAutoplay(true)
                .setCurrentTime(startPosition)
                .build()

            val loadTask = client.load(loadRequest)
            if (startPosition > 0L) {
                loadTask.setResultCallback { result ->
                    if (result.status.isSuccess) {
                        client.seek(
                            MediaSeekOptions.Builder()
                                .setPosition(startPosition * 1000L)
                                .build(),
                        )
                    }
                }
            }

            _castState.update {
                it.copy(
                    hasLoadedVideo = true,
                )
            }
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
        override fun onSessionStarting(session: CastSession) { }

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
