package eu.kanade.tachiyomi.ui.browse.migration.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.MigrateSearchScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.animesource.model.FetchType
import eu.kanade.tachiyomi.ui.anime.AnimeScreen
import eu.kanade.tachiyomi.ui.browse.migration.season.MigrateSeasonSelectScreen
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.SearchViewModel
import mihon.feature.migration.dialog.MigrateAnimeDialog
import mihon.feature.migration.dialog.SelectAnimeDialog
import mihon.feature.migration.list.MigrationListScreen
import tachiyomi.domain.anime.model.Anime

class MigrateSearchScreen(private val animeId: Long) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModel = viewModel<MigrateSearchViewModel>(
            factory = MigrateSearchViewModel.Factory,
            extras = CreationExtras {
                set(MigrateSearchViewModel.ANIME_ID_KEY, animeId)
            },
        )
        val state by viewModel.state.collectAsState()

        // AY -->
        val onSelectAnime: (Anime) -> Unit = {
            val migrateListScreen = navigator.items
                .filterIsInstance<MigrationListScreen>()
                .lastOrNull()

            if (migrateListScreen == null) {
                viewModel.setMigrateDialog(animeId, it)
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
            onChangeSearchQuery = viewModel::updateSearchQuery,
            onSearch = { viewModel.search() },
            getAnime = { viewModel.getAnime(it) },
            onChangeSearchFilter = viewModel::setSourceFilter,
            onToggleResults = viewModel::toggleFilterResults,
            onClickSource = { navigator.push(MigrateSourceSearchScreen(state.from!!, it.id, state.searchQuery)) },
            onClickItem = {
                if (it.fetchType == FetchType.Seasons) {
                    // AY -->
                    viewModel.setSelectDialog(it)
                    // <-- AY
                } else {
                    onSelectAnime(it)
                }
            },
            onLongClickItem = { navigator.push(AnimeScreen(it.id, true)) },
        )

        when (val dialog = state.dialog) {
            is SearchViewModel.Dialog.Migrate -> {
                MigrateAnimeDialog(
                    current = dialog.current,
                    target = dialog.target,
                    // Initiated from the context of [dialog.current] so we show [dialog.target].
                    onClickTitle = { navigator.push(AnimeScreen(dialog.target.id, true)) },
                    // AY -->
                    onClickSeasons = { navigator.push(MigrateSeasonSelectScreen(dialog.current, dialog.target)) },
                    // <-- AY
                    onDismissRequest = { viewModel.clearDialog() },
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
            is SearchViewModel.Dialog.Select -> {
                SelectAnimeDialog(
                    selected = dialog.anime,
                    onDismissRequest = { viewModel.clearDialog() },
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
