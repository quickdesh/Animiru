// AM (CUSTOM_INFORMATION) -->
package tachiyomi.domain.anime.interactor

import dev.zacsweers.metro.Inject
import tachiyomi.domain.anime.repository.CustomAnimeRepository

@Inject
class GetCustomAnimeInfo(
    private val customAnimeRepository: CustomAnimeRepository,
) {
    fun get(animeId: Long) = customAnimeRepository.get(animeId)
}
// <-- AM (CUSTOM_INFORMATION)
