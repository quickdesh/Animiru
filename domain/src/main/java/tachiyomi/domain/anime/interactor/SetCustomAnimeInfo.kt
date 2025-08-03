// AM (CUSTOM_INFORMATION) -->
package tachiyomi.domain.anime.interactor

import tachiyomi.domain.anime.model.CustomAnimeInfo
import tachiyomi.domain.anime.repository.CustomAnimeRepository

class SetCustomAnimeInfo(
    private val customAnimeRepository: CustomAnimeRepository,
) {
    fun set(animeInfo: CustomAnimeInfo) = customAnimeRepository.set(animeInfo)
}
// <-- AM (CUSTOM_INFORMATION)
