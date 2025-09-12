package tachiyomi.domain.source.interactor

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.repository.AnimeRepository
import tachiyomi.domain.source.model.DeletableAnime
import tachiyomi.domain.source.model.SourceWithIds
import tachiyomi.domain.source.repository.SourceRepository

class GetSourcesWithNonLibraryAnime(
    private val repository: AnimeRepository,
) {

    // AY -->
    fun subscribe(): Flow<List<DeletableAnime>> {
        return repository.getDeletableParentAnime()
    }

    suspend fun getDeletableChildren(parentId: Long): List<Anime> {
        return repository.getChildrenByParentId(parentId)
    }
    // <-- AY
}
