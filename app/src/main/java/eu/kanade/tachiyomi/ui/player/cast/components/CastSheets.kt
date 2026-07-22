package eu.kanade.tachiyomi.ui.player.cast.components

import androidx.compose.runtime.Composable
import eu.kanade.tachiyomi.ui.player.cast.CastSheet
import eu.kanade.tachiyomi.ui.player.cast.CastUiData
import eu.kanade.tachiyomi.ui.player.components.HosterState
import eu.kanade.tachiyomi.ui.player.components.QualitySheet
import tachiyomi.cast.domain.TrackInformation

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

    // Tracks sheet
    castUiData: CastUiData,
    onSelectTrack: (TrackInformation, Boolean) -> Unit,

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
        CastSheet.Audio -> {
            CastTrackSheet(
                isAudio = true,
                tracks = castUiData.audioTracks,
                selectedIndex = castUiData.currentAudioId,
                onSelect = { onSelectTrack(it, true) },
                onDismissRequest = onDismissRequest,
            )
        }
        CastSheet.Subtitle -> {
            CastTrackSheet(
                isAudio = false,
                tracks = castUiData.subTracks,
                selectedIndex = castUiData.currentSubId,
                onSelect = { onSelectTrack(it, false) },
                onDismissRequest = onDismissRequest,
            )
        }
    }
}
