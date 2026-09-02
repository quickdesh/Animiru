package tachiyomi.domain.anime.interactor

import dev.zacsweers.metro.Inject
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.repository.AnimeRepository

@Inject
class GetAnimeByUrlAndSourceId(
    private val animeRepository: AnimeRepository,
) {
    suspend fun await(url: String, sourceId: Long): Anime? {
        return animeRepository.getAnimeByUrlAndSourceId(url, sourceId)
    }
}
