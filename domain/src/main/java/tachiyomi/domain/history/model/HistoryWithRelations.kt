package tachiyomi.domain.history.model

import tachiyomi.domain.anime.interactor.GetCustomAnimeInfo
import tachiyomi.domain.anime.model.AnimeCover
import tachiyomi.domain.anime.model.CustomAnimeInfoHolder
import java.util.Date

data class HistoryWithRelations(
    val id: Long,
    val episodeId: Long,
    val animeId: Long,
    // AM (CUSTOM_INFORMATION) -->
    val ogTitle: String,
    // <-- AM (CUSTOM_INFORMATION)
    val episodeNumber: Double,
    val seenAt: Date?,
    val coverData: AnimeCover,
) {
    // AM (CUSTOM_INFORMATION) -->
    val title: String = getCustomAnimeInfo.get(animeId)?.title ?: ogTitle

    companion object {
        private val getCustomAnimeInfo: GetCustomAnimeInfo by lazy {
            CustomAnimeInfoHolder.getCustomAnimeInfo
        }
    }
    // <-- AM (CUSTOM_INFORMATION)
}
