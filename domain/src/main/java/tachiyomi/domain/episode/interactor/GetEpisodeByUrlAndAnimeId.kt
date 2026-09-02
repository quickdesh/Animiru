package tachiyomi.domain.episode.interactor

import dev.zacsweers.metro.Inject
import tachiyomi.domain.episode.model.Episode
import tachiyomi.domain.episode.repository.EpisodeRepository

@Inject
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
