package eu.kanade.tachiyomi.ui.player.cast.components

import androidx.compose.runtime.Composable
import eu.kanade.tachiyomi.ui.player.cast.CastSheet
import eu.kanade.tachiyomi.ui.player.components.HosterState
import eu.kanade.tachiyomi.ui.player.components.QualitySheet

@Composable
fun CastSheets(
    sheetShown: CastSheet,

    // Quality sheet
    isLoadingHosters: Boolean,
    hosterState: List<HosterState>,
    expandedState: List<Boolean>,
    selectedVideoIndex: Pair<Int, Int>,
    onClickHoster: (Int) -> Unit,
    onClickVideo: (Int, Int) -> Unit,
    displayHosters: Pair<Boolean, Boolean>,

    onDismissRequest: () -> Unit,
    dismissSheet: Boolean,
) {
    when (sheetShown) {
        CastSheet.None -> { }
        CastSheet.Quality -> {
            QualitySheet(
                isLoadingHosters = isLoadingHosters,
                hosterState = hosterState,
                expandedState = expandedState,
                selectedVideoIndex = selectedVideoIndex,
                onClickHoster = onClickHoster,
                onClickVideo = onClickVideo,
                displayHosters = displayHosters,
                onDismissRequest = onDismissRequest,
                dismissSheet = dismissSheet,
            )
        }
    }
}
