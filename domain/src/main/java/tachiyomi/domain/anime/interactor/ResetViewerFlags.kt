package tachiyomi.domain.anime.interactor

import tachiyomi.domain.anime.repository.AnimeRepository

class ResetViewerFlags(
    private val animeRepository: AnimeRepository,
) {

    suspend fun await(): Boolean {
        return animeRepository.resetViewerFlags()
    }
}
