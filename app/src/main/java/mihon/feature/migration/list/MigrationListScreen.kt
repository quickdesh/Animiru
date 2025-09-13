package mihon.feature.migration.list

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
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
        val screenModel = rememberScreenModel { MigrationListScreenModel(animeIds, extraSearchQuery) }
        val state by screenModel.state.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(matchOverride) {
            val (current, target) = matchOverride ?: return@LaunchedEffect
            screenModel.useAnimeForMigration(
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

        LaunchedEffect(screenModel) {
            screenModel.navigateBackEvent.collect {
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
            onSkip = { screenModel.removeAnime(it) },
            onMigrate = { screenModel.migrateNow(animeId = it, replace = true) },
            onCopy = { screenModel.migrateNow(animeId = it, replace = false) },
            openMigrationDialog = screenModel::showMigrateDialog,
        )

        when (val dialog = state.dialog) {
            is MigrationListScreenModel.Dialog.Migrate -> {
                MigrationAnimeDialog(
                    onDismissRequest = screenModel::dismissDialog,
                    copy = dialog.copy,
                    totalCount = dialog.totalCount,
                    skippedCount = dialog.skippedCount,
                    onMigrate = {
                        if (dialog.copy) {
                            screenModel.copyAnimes()
                        } else {
                            screenModel.migrateAnimes()
                        }
                    },
                )
            }
            is MigrationListScreenModel.Dialog.Progress -> {
                MigrationProgressDialog(
                    progress = dialog.progress,
                    exitMigration = screenModel::cancelMigrate,
                )
            }
            MigrationListScreenModel.Dialog.Exit -> {
                MigrationExitDialog(
                    onDismissRequest = screenModel::dismissDialog,
                    exitMigration = navigator::pop,
                )
            }
            null -> Unit
        }

        BackHandler(true) {
            screenModel.showExitDialog()
        }
    }
}
