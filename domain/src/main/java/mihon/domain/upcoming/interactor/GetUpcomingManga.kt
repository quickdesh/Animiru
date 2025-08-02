package mihon.domain.upcoming.interactor

import eu.kanade.tachiyomi.animesource.model.SAnime
import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.repository.MangaRepository

class GetUpcomingManga(
    private val mangaRepository: MangaRepository,
) {

    private val includedStatuses = setOf(
        SAnime.ONGOING.toLong(),
        SAnime.PUBLISHING_FINISHED.toLong(),
    )

    suspend fun subscribe(): Flow<List<Manga>> {
        return mangaRepository.getUpcomingManga(includedStatuses)
    }
}
