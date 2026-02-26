package tachiyomi.domain.episode.interactor

import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.episode.model.Episode
import tachiyomi.domain.episode.repository.EpisodeRepository

class GetBookmarkedEpisodesByAnimeId(
    private val episodeRepository: EpisodeRepository,
) {

    suspend fun await(animeId: Long): List<Episode> {
        return try {
            episodeRepository.getBookmarkedEpisodesByAnimeId(animeId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            emptyList()
        }
    }
}
