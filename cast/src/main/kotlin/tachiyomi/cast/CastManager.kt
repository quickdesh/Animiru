package tachiyomi.cast

import android.content.Context
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.Video
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import tachiyomi.domain.anime.model.Anime

interface CastManager {

    val castState: StateFlow<CastState>

    val castEvent: SharedFlow<CastEvent>

    fun initialize(context: Context)

    fun disconnect()

    fun startCasting(
        video: Video,
        source: AnimeSource,
        anime: Anime,
        episodeTitle: String,
        startPosition: Long = 0L,
    )
}
