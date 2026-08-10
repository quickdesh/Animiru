package eu.kanade.tachiyomi.ui.browse.migration.search

import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.SearchItemResult
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.SearchScreenModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tachiyomi.domain.anime.interactor.GetAnime
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MigrateSearchScreenModel(
    val animeId: Long,
    getAnime: GetAnime = Injekt.get(),
    private val sourceManager: SourceManager = Injekt.get(),
    private val sourcePreferences: SourcePreferences = Injekt.get(),
) : SearchScreenModel() {

    private val migrationSources by lazy { sourcePreferences.migrationSources.get() }

    override val sortComparator = { map: Map<AnimeSource, SearchItemResult> ->
        compareBy<AnimeSource>(
            { (map[it] as? SearchItemResult.Success)?.isEmpty ?: true },
            { migrationSources.indexOf(it.id) },
        )
    }

    init {
        screenModelScope.launch {
            val anime = getAnime.await(animeId)!!
            mutableState.update {
                it.copy(
                    from = anime,
                    searchQuery = anime.title,
                )
            }
            search()
        }
    }

    override fun getEnabledSources(): List<AnimeSource> {
        return migrationSources.mapNotNull { sourceManager.get(it) }
    }
}
