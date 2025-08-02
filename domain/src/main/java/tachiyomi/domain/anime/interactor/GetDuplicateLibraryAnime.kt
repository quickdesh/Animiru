package tachiyomi.domain.anime.interactor

import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.model.AnimeWithEpisodeCount
import tachiyomi.domain.anime.repository.AnimeRepository

class GetDuplicateLibraryAnime(
    private val animeRepository: AnimeRepository,
) {

    suspend operator fun invoke(anime: Anime): List<AnimeWithEpisodeCount> {
        return animeRepository.getDuplicateLibraryAnime(anime.id, anime.title.lowercase())
    }
}
