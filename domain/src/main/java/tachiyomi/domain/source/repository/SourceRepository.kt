package tachiyomi.domain.source.repository

import androidx.paging.PagingSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.source.model.Source
import tachiyomi.domain.source.model.SourceWithIds

typealias SourcePagingSource = PagingSource<Long, Anime>

interface SourceRepository {

    fun getSources(): Flow<List<Source>>

    fun getOnlineSources(): Flow<List<Source>>

    fun getSourcesWithFavoriteCount(): Flow<List<Pair<Source, Long>>>

    fun search(sourceId: Long, query: String, filterList: AnimeFilterList): SourcePagingSource

    fun getPopular(sourceId: Long): SourcePagingSource

    fun getLatest(sourceId: Long): SourcePagingSource
}
