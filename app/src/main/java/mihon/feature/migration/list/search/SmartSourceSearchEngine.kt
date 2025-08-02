package mihon.feature.migration.list.search

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.SAnime
import mihon.domain.manga.model.toDomainManga
import tachiyomi.domain.manga.model.Manga

class SmartSourceSearchEngine(extraSearchParams: String?) : BaseSmartSearchEngine<SAnime>(extraSearchParams) {

    override fun getTitle(result: SAnime) = result.title

    suspend fun regularSearch(source: AnimeCatalogueSource, title: String): Manga? {
        return regularSearch(makeSearchAction(source), title).let {
            it?.toDomainManga(source.id)
        }
    }

    suspend fun deepSearch(source: AnimeCatalogueSource, title: String): Manga? {
        return deepSearch(makeSearchAction(source), title).let {
            it?.toDomainManga(source.id)
        }
    }

    private fun makeSearchAction(source: AnimeCatalogueSource): SearchAction<SAnime> = { query ->
        source.getSearchAnime(1, query, AnimeFilterList()).animes
    }
}
