package eu.kanade.tachiyomi.ui.browse.migration.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.MigrateSearchScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.animesource.model.FetchType
import eu.kanade.tachiyomi.ui.anime.AnimeScreen
import eu.kanade.tachiyomi.ui.browse.migration.season.MigrateSeasonSelectScreen
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.SearchScreenModel
import mihon.feature.migration.dialog.MigrateAnimeDialog
import mihon.feature.migration.dialog.SelectAnimeDialog
import mihon.feature.migration.list.MigrationListScreen
import tachiyomi.domain.anime.model.Anime

class MigrateSearchScreen(private val animeId: Long) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = rememberScreenModel { MigrateSearchScreenModel(animeId = animeId) }
        val state by screenModel.state.collectAsState()

        // AY -->
        val onSelectAnime: (Anime) -> Unit = {
            val migrateListScreen = navigator.items
                .filterIsInstance<MigrationListScreen>()
                .lastOrNull()

            if (migrateListScreen == null) {
                screenModel.setMigrateDialog(animeId, it)
            } else {
                migrateListScreen.addMatchOverride(current = animeId, target = it.id)
                navigator.popUntil { screen -> screen is MigrationListScreen }
            }
        }
        // <-- AY

        MigrateSearchScreen(
            state = state,
            fromSourceId = state.from?.source,
            navigateUp = navigator::pop,
            onChangeSearchQuery = screenModel::updateSearchQuery,
            onSearch = { screenModel.search() },
            getAnime = { screenModel.getAnime(it) },
            onChangeSearchFilter = screenModel::setSourceFilter,
            onToggleResults = screenModel::toggleFilterResults,
            onClickSource = { navigator.push(MigrateSourceSearchScreen(state.from!!, it.id, state.searchQuery)) },
            onClickItem = {
                if (it.fetchType == FetchType.Seasons) {
                    // AY -->
                    screenModel.setSelectDialog(it)
                    // <-- AY
                } else {
                    onSelectAnime(it)
                }
            },
            onLongClickItem = { navigator.push(AnimeScreen(it.id, true)) },
        )

        when (val dialog = state.dialog) {
            is SearchScreenModel.Dialog.Migrate -> {
                MigrateAnimeDialog(
                    current = dialog.current,
                    target = dialog.target,
                    // Initiated from the context of [dialog.current] so we show [dialog.target].
                    onClickTitle = { navigator.push(AnimeScreen(dialog.target.id, true)) },
                    // AY -->
                    onClickSeasons = { navigator.push(MigrateSeasonSelectScreen(dialog.current, dialog.target)) },
                    // <-- AY
                    onDismissRequest = { screenModel.clearDialog() },
                    onComplete = {
                        if (navigator.lastItem is AnimeScreen) {
                            val lastItem = navigator.lastItem
                            navigator.popUntil { navigator.items.contains(lastItem) }
                            navigator.push(AnimeScreen(dialog.target.id))
                        } else {
                            navigator.replace(AnimeScreen(dialog.target.id))
                        }
                    },
                )
            }
            is SearchScreenModel.Dialog.Select -> {
                SelectAnimeDialog(
                    selected = dialog.anime,
                    onDismissRequest = { screenModel.clearDialog() },
                    onClickTitle = { navigator.push(AnimeScreen(dialog.anime.id)) },
                    onClickSeasons = {
                        val isFromList = navigator.items.any { it is MigrationListScreen }
                        navigator.push(MigrateSeasonSelectScreen(state.from!!, dialog.anime, isFromList))
                    },
                    onClickSelect = { onSelectAnime(dialog.anime) },
                )
            }
            else -> {}
        }
    }
}
