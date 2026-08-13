package eu.kanade.presentation.updates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FlipToBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import animiru.domain.player.service.PlayerPreferences
import eu.kanade.presentation.anime.components.AnimeBottomActionMenu
import eu.kanade.presentation.anime.components.EpisodeDownloadAction
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.ui.updates.UpdatesItem
import eu.kanade.tachiyomi.ui.updates.UpdatesScreenModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.theme.active
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

// AM (RECENTS_FILTER_CHIP) -->
@Composable
fun UpdateScreen(
    state: UpdatesScreenModel.State,
    lastUpdated: Long,
    onClickCover: (UpdatesItem) -> Unit,
    onUpdateLibrary: () -> Boolean,
    onDownloadEpisode: (List<UpdatesItem>, EpisodeDownloadAction) -> Unit,
    onUpdateSelected: (UpdatesItem, Boolean, Boolean) -> Unit,
    onOpenEpisode: (UpdatesItem, altPlayer: Boolean) -> Unit,
    contentPadding: PaddingValues,
) {
    when {
        state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
        state.items.isEmpty() -> EmptyScreen(
            stringRes = MR.strings.information_no_recent,
            modifier = Modifier.padding(contentPadding),
        )
        else -> {
            val scope = rememberCoroutineScope()
            var isRefreshing by remember { mutableStateOf(false) }

            PullRefresh(
                refreshing = isRefreshing,
                onRefresh = {
                    val started = onUpdateLibrary()
                    if (!started) return@PullRefresh
                    scope.launch {
                        // Fake refresh status but hide it after a second as it's a long running task
                        isRefreshing = true
                        delay(1.seconds)
                        isRefreshing = false
                    }
                },
                enabled = !state.selectionMode,
                indicatorPadding = contentPadding,
            ) {
                FastScrollLazyColumn(
                    contentPadding = contentPadding,
                ) {
                    updatesLastUpdatedItem(lastUpdated)

                    updatesUiItems(
                        uiModels = state.getUiModel(),
                        selectionMode = state.selectionMode,
                        onUpdateSelected = onUpdateSelected,
                        onClickCover = onClickCover,
                        onClickUpdate = onOpenEpisode,
                        onDownloadEpisode = onDownloadEpisode,
                    )
                }
            }
        }
    }
}

@Composable
fun UpdatesTopBar(
    onCalendarClicked: () -> Unit,
    onUpdateLibrary: () -> Unit,
    onFilterClicked: () -> Unit,
    hasFilters: Boolean,
    // For action mode
    actionModeCounter: Int,
    onSelectAll: () -> Unit,
    onInvertSelection: () -> Unit,
    onCancelActionMode: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    AppBar(
        modifier = modifier,
        title = stringResource(MR.strings.label_recent_updates),
        actions = {
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_filter),
                        icon = Icons.Outlined.FilterList,
                        iconTint = if (hasFilters) MaterialTheme.colorScheme.active else LocalContentColor.current,
                        onClick = onFilterClicked,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_view_upcoming),
                        icon = Icons.Outlined.CalendarMonth,
                        onClick = onCalendarClicked,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_update_library),
                        icon = Icons.Outlined.Refresh,
                        onClick = onUpdateLibrary,
                    ),
                ),
            )
        },
        actionModeCounter = actionModeCounter,
        onCancelActionMode = onCancelActionMode,
        actionModeActions = {
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_all),
                        icon = Icons.Outlined.SelectAll,
                        onClick = onSelectAll,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_inverse),
                        icon = Icons.Outlined.FlipToBack,
                        onClick = onInvertSelection,
                    ),
                ),
            )
        },
        scrollBehavior = scrollBehavior,
    )
}
// <-- AM (RECENTS_FILTER_CHIP)

@Composable
fun UpdatesBottomBar(
    selected: List<UpdatesItem>,
    onDownloadEpisode: (List<UpdatesItem>, EpisodeDownloadAction) -> Unit,
    onMultiBookmarkClicked: (List<UpdatesItem>, bookmark: Boolean) -> Unit,
    // AY -->
    onMultiFillermarkClicked: (List<UpdatesItem>, fillermarked: Boolean) -> Unit,
    // <-- AY
    onMultiMarkAsSeenClicked: (List<UpdatesItem>, seen: Boolean) -> Unit,
    onMultiDeleteClicked: (List<UpdatesItem>) -> Unit,
    onOpenEpisode: (UpdatesItem, altPlayer: Boolean) -> Unit,
) {
    val playerPreferences: PlayerPreferences = Injekt.get()
    AnimeBottomActionMenu(
        visible = selected.isNotEmpty(),
        modifier = Modifier.fillMaxWidth(),
        onBookmarkClicked = {
            onMultiBookmarkClicked.invoke(selected, true)
        }.takeIf { selected.fastAny { !it.update.bookmark } },
        onRemoveBookmarkClicked = {
            onMultiBookmarkClicked.invoke(selected, false)
        }.takeIf { selected.fastAll { it.update.bookmark } },
        // AY -->
        onFillermarkClicked = {
            onMultiFillermarkClicked.invoke(selected, true)
        }.takeIf { selected.fastAny { !it.update.fillermark } },
        onRemoveFillermarkClicked = {
            onMultiFillermarkClicked.invoke(selected, false)
        }.takeIf { selected.fastAll { it.update.fillermark } },
        // <-- AY
        onMarkAsSeenClicked = {
            onMultiMarkAsSeenClicked(selected, true)
        }.takeIf { selected.fastAny { !it.update.seen } },
        onMarkAsUnseenClicked = {
            onMultiMarkAsSeenClicked(selected, false)
        }.takeIf { selected.fastAny { it.update.seen || it.update.lastSecondSeen > 0L } },
        onDownloadClicked = {
            onDownloadEpisode(selected, EpisodeDownloadAction.START)
        }.takeIf {
            selected.fastAny { it.downloadStateProvider() != Download.State.DOWNLOADED }
        },
        onDeleteClicked = {
            onMultiDeleteClicked(selected)
        }.takeIf { selected.fastAny { it.downloadStateProvider() == Download.State.DOWNLOADED } },
        onExternalClicked = {
            onOpenEpisode(selected[0], true)
        }.takeIf { !playerPreferences.alwaysUseExternalPlayer.get() && selected.size == 1 },
        onInternalClicked = {
            onOpenEpisode(selected[0], true)
        }.takeIf { playerPreferences.alwaysUseExternalPlayer.get() && selected.size == 1 },
    )
}

sealed interface UpdatesUiModel {
    data class Header(val date: LocalDate) : UpdatesUiModel
    data class Item(val item: UpdatesItem) : UpdatesUiModel
}
