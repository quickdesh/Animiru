package eu.kanade.tachiyomi.ui.updates

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import animiru.domain.player.service.PlayerPreferences
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.anime.EpisodeOptionsDialogScreen
import eu.kanade.presentation.components.NavigatorAdaptiveSheet
import eu.kanade.presentation.updates.UpdateScreen
import eu.kanade.presentation.updates.UpdatesDeleteConfirmationDialog
import eu.kanade.presentation.updates.UpdatesFilterDialog
import eu.kanade.tachiyomi.ui.anime.AnimeScreen
import eu.kanade.tachiyomi.ui.home.HomeScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.updates.UpdatesScreenModel.Event
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.i18n.MR
import uy.kohesive.injekt.injectLazy

// AM (RECENTS_FILTER_CHIP) -->
@Composable
fun AnimeUpdatesHalfTab(
    screenModel: UpdatesScreenModel,
    settingsScreenModel: UpdatesSettingsScreenModel,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()
    val state by screenModel.state.collectAsState()

    UpdateScreen(
        state = state,
        lastUpdated = screenModel.lastUpdated,
        onClickCover = { item -> navigator.push(AnimeScreen(item.update.animeId)) },
        onUpdateLibrary = screenModel::updateLibrary,
        onDownloadEpisode = screenModel::downloadEpisodes,
        onUpdateSelected = screenModel::toggleSelection,
        onOpenEpisode = { updateItem: UpdatesItem, altPlayer: Boolean ->
            scope.launchIO {
                openEpisode(context, updateItem, altPlayer)
            }
            Unit
        },
        contentPadding = contentPadding,
    )
    // <-- AM (RECENTS_FILTER_CHIP)

    val onDismissDialog = { screenModel.setDialog(null) }
    when (val dialog = state.dialog) {
        is UpdatesScreenModel.Dialog.DeleteConfirmation -> {
            UpdatesDeleteConfirmationDialog(
                onDismissRequest = onDismissDialog,
                onConfirm = { screenModel.deleteEpisodes(dialog.toDelete) },
            )
        }
        is UpdatesScreenModel.Dialog.FilterSheet -> {
            UpdatesFilterDialog(
                onDismissRequest = onDismissDialog,
                screenModel = settingsScreenModel,
            )
        }
        is UpdatesScreenModel.Dialog.ShowQualities -> {
            EpisodeOptionsDialogScreen.onDismissDialog = onDismissDialog
            NavigatorAdaptiveSheet(
                screen = EpisodeOptionsDialogScreen(
                    useExternalDownloader = screenModel.useExternalDownloader,
                    episodeTitle = dialog.episodeTitle,
                    episodeId = dialog.episodeId,
                    animeId = dialog.animeId,
                    sourceId = dialog.sourceId,
                ),
                onDismissRequest = onDismissDialog,
            )
        }
        null -> {}
    }

    LaunchedEffect(Unit) {
        screenModel.events.collectLatest { event ->
            when (event) {
                Event.InternalError -> screenModel.snackbarHostState.showSnackbar(
                    context.stringResource(MR.strings.internal_error),
                )
                is Event.LibraryUpdateTriggered -> {
                    val msg = if (event.started) {
                        MR.strings.updating_library
                    } else {
                        MR.strings.update_already_running
                    }
                    screenModel.snackbarHostState.showSnackbar(context.stringResource(msg))
                }
            }
        }
    }

    LaunchedEffect(state.selectionMode) {
        HomeScreen.showBottomNav(!state.selectionMode)
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            (context as? MainActivity)?.ready = true
        }
    }
    DisposableEffect(Unit) {
        screenModel.resetNewUpdatesCount()

        onDispose {
            screenModel.resetNewUpdatesCount()
        }
    }
}

suspend fun openEpisode(context: Context, updateItem: UpdatesItem, altPlayer: Boolean = false) {
    val playerPreferences: PlayerPreferences by injectLazy()
    val update = updateItem.update
    val extPlayer = playerPreferences.alwaysUseExternalPlayer.get() != altPlayer
    MainActivity.startPlayerActivity(context, update.animeId, update.episodeId, extPlayer, update.sourceId)
}
