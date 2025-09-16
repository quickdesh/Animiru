package tachiyomi.domain.updates.model

import tachiyomi.domain.anime.interactor.GetCustomAnimeInfo
import tachiyomi.domain.anime.model.AnimeCover
import uy.kohesive.injekt.injectLazy

data class UpdatesWithRelations(
    val animeId: Long,
    // AM (CUSTOM_INFORMATION) -->
    val ogAnimeTitle: String,
    // <-- AM (CUSTOM_INFORMATION)
    val episodeId: Long,
    val episodeName: String,
    val scanlator: String?,
    val seen: Boolean,
    val bookmark: Boolean,
    // AY -->
    val fillermark: Boolean,
    // <-- AY
    val lastSecondSeen: Long,
    // AY -->
    val totalSeconds: Long,
    // <-- AY
    val sourceId: Long,
    val dateFetch: Long,
    val coverData: AnimeCover,
) {
    // AM (CUSTOM_INFORMATION) -->
    val animeTitle: String = getCustomAnimeInfo.get(animeId)?.title ?: ogAnimeTitle

    companion object {
        private val getCustomAnimeInfo: GetCustomAnimeInfo by injectLazy()
    }
    // <-- AM (CUSTOM_INFORMATION)
}
