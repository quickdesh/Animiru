package tachiyomi.cast

import android.content.Context
import eu.kanade.tachiyomi.animesource.model.Video
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import tachiyomi.cast.domain.CodecInformation
import tachiyomi.cast.domain.TrackInformation
import tachiyomi.domain.anime.model.Anime

class CastManagerImpl : CastManager {

    override val castState = MutableStateFlow(CastState())

    override val castEvent = MutableSharedFlow<CastEvent>()

    override fun initialize(context: Context) {
        // NOOP
    }

    override fun disconnect() {
        // NOOP
    }

    override fun startCasting(
        video: Video,
        videoInformation: CodecInformation,
        subtitleTracks: List<TrackInformation>,
        audioTracks: List<TrackInformation>,
        subtitleId: Long?,
        audioId: Long?,
        anime: Anime,
        episodeTitle: String,
        startPosition: Long,
        playbackRate: Double,
    ) {
        // NOOP
    }

    override fun seekTo(position: Long) {
        // NOOP
    }

    override fun seekBy(delta: Long) {
        // NOOP
    }

    override fun setSpeed(speed: Double) {
        // NOOP
    }

    override fun loadTrack(trackId: Long, isAudio: Boolean) {
        // NOOP
    }

    override fun stopRemoteMediaClient() {
        // NOOP
    }

    override fun handleCastManagerEvent(event: CastManagerEvent) {
        // NOOP
    }
}
