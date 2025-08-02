package mihon.domain.upcoming.interactor

import eu.kanade.tachiyomi.animesource.model.SAnime
import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.repository.AnimeRepository

class GetUpcomingAnime(
    private val animeRepository: AnimeRepository,
) {

    private val includedStatuses = setOf(
        SAnime.ONGOING.toLong(),
        SAnime.PUBLISHING_FINISHED.toLong(),
    )

    suspend fun subscribe(): Flow<List<Anime>> {
        return animeRepository.getUpcomingAnime(includedStatuses)
    }
}
