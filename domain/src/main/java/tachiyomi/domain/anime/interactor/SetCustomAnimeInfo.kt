// AM (CUSTOM_INFORMATION) -->
package tachiyomi.domain.anime.interactor

import dev.zacsweers.metro.Inject
import tachiyomi.domain.anime.model.CustomAnimeInfo
import tachiyomi.domain.anime.repository.CustomAnimeRepository

@Inject
class SetCustomAnimeInfo(
    private val customAnimeRepository: CustomAnimeRepository,
) {
    fun set(animeInfo: CustomAnimeInfo) = customAnimeRepository.set(animeInfo)
}
// <-- AM (CUSTOM_INFORMATION)
