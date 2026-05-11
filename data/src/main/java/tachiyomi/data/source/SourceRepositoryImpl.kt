package tachiyomi.data.source

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import tachiyomi.data.Database
import tachiyomi.data.subscribeToList
import tachiyomi.domain.source.model.StubSource
import tachiyomi.domain.source.repository.SourcePagingSource
import tachiyomi.domain.source.repository.SourceRepository
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.domain.source.model.Source as DomainSource

class SourceRepositoryImpl(
    private val sourceManager: SourceManager,
    private val database: Database,
) : SourceRepository {

    override fun getSources(): Flow<List<DomainSource>> {
        return sourceManager.catalogueSources.map { sources ->
            sources.map {
                mapSourceToDomainSource(it).copy(
                    supportsLatest = it.supportsLatest,
                )
            }
        }
    }

    override fun getOnlineSources(): Flow<List<DomainSource>> {
        return sourceManager.catalogueSources.map { sources ->
            sources
                .filterIsInstance<AnimeHttpSource>()
                .map(::mapSourceToDomainSource)
        }
    }

    override fun getSourcesWithFavoriteCount(): Flow<List<Pair<DomainSource, Long>>> {
        val sourceIdWithFavoriteCountFlow = database.animesQueries
            .getSourceIdWithFavoriteCount()
            .subscribeToList()

        return combine(sourceIdWithFavoriteCountFlow, sourceManager.catalogueSources) { sourceIdWithFavoriteCount, _ ->
            sourceIdWithFavoriteCount
        }
            .map {
                it.map { (sourceId, count) ->
                    val source = sourceManager.getOrStub(sourceId)
                    val domainSource = mapSourceToDomainSource(source).copy(
                        isStub = source is StubSource,
                    )
                    domainSource to count
                }
            }
    }

    override fun search(
        sourceId: Long,
        query: String,
        filterList: AnimeFilterList,
    ): SourcePagingSource {
        val source = sourceManager.get(sourceId) as AnimeCatalogueSource
        return SourceSearchPagingSource(source, query, filterList)
    }

    override fun getPopular(sourceId: Long): SourcePagingSource {
        val source = sourceManager.get(sourceId) as AnimeCatalogueSource
        return SourcePopularPagingSource(source)
    }

    override fun getLatest(sourceId: Long): SourcePagingSource {
        val source = sourceManager.get(sourceId) as AnimeCatalogueSource
        return SourceLatestPagingSource(source)
    }
}

fun mapSourceToDomainSource(source: AnimeSource): DomainSource = DomainSource(
    id = source.id,
    lang = source.lang,
    name = source.name,
    supportsLatest = false,
    isStub = false,
)
