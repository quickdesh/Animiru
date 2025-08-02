package tachiyomi.data.source

import androidx.paging.PagingState
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import mihon.domain.manga.model.toDomainManga
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.repository.SourcePagingSource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class SourceSearchPagingSource(
    source: AnimeCatalogueSource,
    private val query: String,
    private val filters: AnimeFilterList,
) : BaseSourcePagingSource(source) {
    override suspend fun requestNextPage(currentPage: Int): AnimesPage {
        return source.getSearchAnime(currentPage, query, filters)
    }
}

class SourcePopularPagingSource(source: AnimeCatalogueSource) : BaseSourcePagingSource(source) {
    override suspend fun requestNextPage(currentPage: Int): AnimesPage {
        return source.getPopularAnime(currentPage)
    }
}

class SourceLatestPagingSource(source: AnimeCatalogueSource) : BaseSourcePagingSource(source) {
    override suspend fun requestNextPage(currentPage: Int): AnimesPage {
        return source.getLatestUpdates(currentPage)
    }
}

abstract class BaseSourcePagingSource(
    protected val source: AnimeCatalogueSource,
    private val networkToLocalManga: NetworkToLocalManga = Injekt.get(),
) : SourcePagingSource() {

    private val seenManga = hashSetOf<String>()

    abstract suspend fun requestNextPage(currentPage: Int): AnimesPage

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Manga> {
        val page = params.key ?: 1

        return try {
            val mangasPage = withIOContext {
                requestNextPage(page.toInt())
                    .takeIf { it.animes.isNotEmpty() }
                    ?: throw NoResultsException()
            }

            val manga = mangasPage.animes
                .map { it.toDomainManga(source.id) }
                .filter { seenManga.add(it.url) }
                .let { networkToLocalManga(it) }

            LoadResult.Page(
                data = manga,
                prevKey = null,
                nextKey = if (mangasPage.hasNextPage) page + 1 else null,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Long, Manga>): Long? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}

class NoResultsException : Exception()
