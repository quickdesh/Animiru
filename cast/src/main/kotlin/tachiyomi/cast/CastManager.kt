package tachiyomi.cast

import android.content.Context
import eu.kanade.tachiyomi.animesource.model.Video
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import tachiyomi.cast.domain.CodecInformation
import tachiyomi.cast.domain.TrackInformation
import tachiyomi.domain.anime.model.Anime

interface CastManager {

    val castState: StateFlow<CastState>

    val castEvent: SharedFlow<CastEvent>

    fun initialize(context: Context)

    fun disconnect()

    fun startCasting(
        video: Video,
        videoInformation: CodecInformation,
        subtitleTracks: List<TrackInformation>,
        audioTracks: List<TrackInformation>,
        anime: Anime,
        episodeTitle: String,
        startPosition: Long = 0L,
    )

    fun stopRemoteMediaClient()

    fun handleCastManagerEvent(event: CastManagerEvent)
}
