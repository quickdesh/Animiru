package eu.kanade.tachiyomi.ui.history

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import animiru.domain.player.service.PlayerPreferences
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.anime.DuplicateAnimeDialog
import eu.kanade.presentation.category.components.ChangeCategoryDialog
import eu.kanade.presentation.history.HistoryScreen
import eu.kanade.presentation.history.components.HistoryDeleteAllDialog
import eu.kanade.presentation.history.components.HistoryDeleteDialog
import eu.kanade.tachiyomi.ui.anime.AnimeScreen
import eu.kanade.tachiyomi.ui.browse.migration.season.MigrateSeasonSelectScreen
import eu.kanade.tachiyomi.ui.category.CategoryScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import mihon.feature.migration.dialog.MigrateAnimeDialog
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.episode.model.Episode
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import uy.kohesive.injekt.injectLazy

val resumeLastEpisodeSeenEvent = Channel<Unit>()

// AM (RECENTS_FILTER_CHIP) -->
@Composable
fun Screen.HistoryHalfTab(
    viewModel: HistoryViewModel,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val state by viewModel.state.collectAsState()

    HistoryScreen(
        state = state,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onClickCover = { navigator.push(AnimeScreen(it)) },
        onClickResume = viewModel::getNextEpisodeForAnime,
        onDialogChange = viewModel::setDialog,
        onClickFavorite = viewModel::addFavorite,
        contentPadding = contentPadding,
    )
    // <-- AM (RECENTS_FILTER_CHIP)

    val onDismissRequest = { viewModel.setDialog(null) }
    when (val dialog = state.dialog) {
        is HistoryViewModel.Dialog.Delete -> {
            HistoryDeleteDialog(
                onDismissRequest = onDismissRequest,
                onDelete = { all ->
                    if (all) {
                        viewModel.removeAllFromHistory(dialog.history.animeId)
                    } else {
                        viewModel.removeFromHistory(dialog.history)
                    }
                },
            )
        }
        is HistoryViewModel.Dialog.DeleteAll -> {
            HistoryDeleteAllDialog(
                onDismissRequest = onDismissRequest,
                onDelete = viewModel::removeAllHistory,
            )
        }
        is HistoryViewModel.Dialog.DuplicateAnime -> {
            DuplicateAnimeDialog(
                duplicates = dialog.duplicates,
                onDismissRequest = onDismissRequest,
                onConfirm = { viewModel.addFavorite(dialog.anime) },
                onOpenAnime = { navigator.push(AnimeScreen(it.id)) },
                onMigrate = { viewModel.showMigrateDialog(dialog.anime, it) },
            )
        }
        is HistoryViewModel.Dialog.ChangeCategory -> {
            ChangeCategoryDialog(
                initialSelection = dialog.initialSelection,
                onDismissRequest = onDismissRequest,
                onEditCategories = { navigator.push(CategoryScreen()) },
                onConfirm = { include, _ ->
                    viewModel.moveAnimeToCategoriesAndAddToLibrary(dialog.anime, include)
                },
            )
        }
        is HistoryViewModel.Dialog.Migrate -> {
            MigrateAnimeDialog(
                current = dialog.current,
                target = dialog.target,
                // Initiated from the context of [dialog.target] so we show [dialog.current].
                onClickTitle = { navigator.push(AnimeScreen(dialog.current.id)) },
                // AY -->
                onClickSeasons = { navigator.push(MigrateSeasonSelectScreen(dialog.current, dialog.target)) },
                // <-- AY
                onDismissRequest = onDismissRequest,
            )
        }
        null -> {}
    }

    LaunchedEffect(state.list) {
        if (state.list != null) {
            (context as? MainActivity)?.ready = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { e ->
            when (e) {
                HistoryViewModel.Event.InternalError ->
                    snackbarHostState.showSnackbar(context.stringResource(MR.strings.internal_error))
                HistoryViewModel.Event.HistoryCleared ->
                    snackbarHostState.showSnackbar(context.stringResource(MR.strings.clear_history_completed))
                is HistoryViewModel.Event.OpenEpisode -> openEpisode(context, e.episode, snackbarHostState)
            }
        }
    }
}

suspend fun openEpisode(context: Context, episode: Episode?, snackbarHostState: SnackbarHostState) {
    val playerPreferences: PlayerPreferences by injectLazy()
    val extPlayer = playerPreferences.alwaysUseExternalPlayer.get()
    if (episode != null) {
        MainActivity.startPlayerActivity(context, episode.animeId, episode.id, extPlayer)
    } else {
        snackbarHostState.showSnackbar(context.stringResource(AYMR.strings.no_next_episode))
    }
}
