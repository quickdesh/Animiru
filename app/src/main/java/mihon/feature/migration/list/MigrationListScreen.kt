package mihon.feature.migration.list

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.animesource.model.FetchType
import eu.kanade.tachiyomi.ui.anime.AnimeScreen
import eu.kanade.tachiyomi.ui.browse.migration.search.MigrateSearchScreen
import eu.kanade.tachiyomi.ui.browse.migration.season.MigrateSeasonSelectScreen
import eu.kanade.tachiyomi.util.system.toast
import mihon.feature.migration.list.components.MigrationAnimeDialog
import mihon.feature.migration.list.components.MigrationExitDialog
import mihon.feature.migration.list.components.MigrationProgressDialog
import tachiyomi.i18n.animiru.AMMR

class MigrationListScreen(private val animeIds: Collection<Long>, private val extraSearchQuery: String?) : Screen() {

    private var matchOverride: Pair<Long, Long>? = null

    fun addMatchOverride(current: Long, target: Long) {
        matchOverride = current to target
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = viewModel<MigrationListViewModel>(
            factory = MigrationListViewModel.Factory,
            extras = CreationExtras {
                set(MigrationListViewModel.ANIME_IDS_KEY, animeIds)
                set(MigrationListViewModel.EXTRA_SEARCH_QUERY_KEY, extraSearchQuery)
            },
        )
        val state by viewModel.state.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(matchOverride) {
            val (current, target) = matchOverride ?: return@LaunchedEffect
            viewModel.useAnimeForMigration(
                current = current,
                target = target,
                onMissingEntries = {
                    // AY -->
                    val stringResource = when (it) {
                        FetchType.Seasons -> AMMR.strings.am_migrationListScreen_matchWithoutSeasonToast
                        FetchType.Episodes -> AMMR.strings.am_migrationListScreen_matchWithoutEpisodeToast
                    }
                    // <-- AY
                    context.toast(stringResource, Toast.LENGTH_LONG)
                },
            )
            matchOverride = null
        }

        LaunchedEffect(viewModel) {
            viewModel.navigateBackEvent.collect {
                navigator.pop()
            }
        }
        MigrationListScreenContent(
            items = state.items,
            migrationComplete = state.migrationComplete,
            finishedCount = state.finishedCount,
            onItemClick = {
                navigator.push(AnimeScreen(it.id, true))
            },
            onSearchManually = { migrationItem ->
                navigator push MigrateSearchScreen(migrationItem.anime.id)
            },
            // AY -->
            onSearchSeasons = { current, target ->
                navigator push MigrateSeasonSelectScreen(current, target, true)
            },
            // <-- AY
            onSkip = { viewModel.removeAnime(it) },
            onMigrate = { viewModel.migrateNow(animeId = it, replace = true) },
            onCopy = { viewModel.migrateNow(animeId = it, replace = false) },
            openMigrationDialog = viewModel::showMigrateDialog,
        )

        when (val dialog = state.dialog) {
            is MigrationListViewModel.Dialog.Migrate -> {
                MigrationAnimeDialog(
                    onDismissRequest = viewModel::dismissDialog,
                    copy = dialog.copy,
                    totalCount = dialog.totalCount,
                    skippedCount = dialog.skippedCount,
                    onMigrate = {
                        if (dialog.copy) {
                            viewModel.copyAnimes()
                        } else {
                            viewModel.migrateAnimes()
                        }
                    },
                )
            }
            is MigrationListViewModel.Dialog.Progress -> {
                MigrationProgressDialog(
                    progress = dialog.progress,
                    exitMigration = viewModel::cancelMigrate,
                )
            }
            MigrationListViewModel.Dialog.Exit -> {
                MigrationExitDialog(
                    onDismissRequest = viewModel::dismissDialog,
                    exitMigration = navigator::pop,
                )
            }
            null -> Unit
        }

        BackHandler(true) {
            viewModel.showExitDialog()
        }
    }
}
