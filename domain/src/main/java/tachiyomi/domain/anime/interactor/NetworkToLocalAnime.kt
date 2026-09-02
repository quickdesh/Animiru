package tachiyomi.domain.anime.interactor

import dev.zacsweers.metro.Inject
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.repository.AnimeRepository

@Inject
class NetworkToLocalAnime(
    private val animeRepository: AnimeRepository,
) {

    suspend operator fun invoke(anime: Anime): Anime {
        return invoke(listOf(anime)).single()
    }

    suspend operator fun invoke(anime: List<Anime>): List<Anime> {
        return animeRepository.insertNetworkAnime(anime)
    }
}
