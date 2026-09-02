package mihon.domain.upcoming.interactor

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.animesource.model.SAnime
import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.repository.AnimeRepository

@Inject
class GetUpcomingAnime(
    private val animeRepository: AnimeRepository,
) {

    private val includedStatuses = setOf(
        SAnime.ONGOING.toLong(),
        SAnime.PUBLISHING_FINISHED.toLong(),
        SAnime.UPCOMING.toLong(),
    )

    suspend fun subscribe(
        excludedCategories: List<Long>,
        includedCategories: List<Long>,
    ): Flow<List<Anime>> {
        return animeRepository.getUpcomingAnime(
            includedStatuses,
            excludedCategories = excludedCategories,
            includedCategories = includedCategories,
        )
    }
}
