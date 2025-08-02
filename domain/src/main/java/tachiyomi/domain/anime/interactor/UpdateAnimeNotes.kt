package tachiyomi.domain.anime.interactor

import tachiyomi.domain.anime.model.AnimeUpdate
import tachiyomi.domain.anime.repository.AnimeRepository

class UpdateAnimeNotes(
    private val animeRepository: AnimeRepository,
) {

    suspend operator fun invoke(animeId: Long, notes: String): Boolean {
        return animeRepository.update(
            AnimeUpdate(
                id = animeId,
                notes = notes,
            ),
        )
    }
}
