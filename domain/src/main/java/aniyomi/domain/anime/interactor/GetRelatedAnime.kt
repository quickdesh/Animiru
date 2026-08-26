package aniyomi.domain.anime.interactor

import aniyomi.domain.anime.model.AnimeRelationGroup
import aniyomi.domain.anime.repository.AnimeRelationRepository
import kotlinx.coroutines.flow.Flow

class GetRelatedAnime(
    private val relationRepository: AnimeRelationRepository,
) {
    fun subscribe(animeId: Long): Flow<List<AnimeRelationGroup>> {
        return relationRepository.subscribeRelatedAnime(animeId)
    }
}
