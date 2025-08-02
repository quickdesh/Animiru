package tachiyomi.domain.episode.interactor

import tachiyomi.domain.episode.model.Episode
import tachiyomi.domain.episode.repository.EpisodeRepository

class GetEpisodeByUrlAndAnimeId(
    private val episodeRepository: EpisodeRepository,
) {

    suspend fun await(url: String, sourceId: Long): Episode? {
        return try {
            episodeRepository.getEpisodeByUrlAndAnimeId(url, sourceId)
        } catch (e: Exception) {
            null
        }
    }
}
