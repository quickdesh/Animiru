// AY -->
package tachiyomi.domain.season.interactor

import aniyomi.domain.anime.model.SeasonAnime
import dev.zacsweers.metro.Inject
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.anime.repository.AnimeRepository

@Inject
class GetAnimeSeasonsByParentId(
    private val animeRepository: AnimeRepository,
) {
    suspend fun await(animeId: Long): List<SeasonAnime> {
        return try {
            animeRepository.getAnimeSeasonsById(animeId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            emptyList()
        }
    }
}
// <-- AY
