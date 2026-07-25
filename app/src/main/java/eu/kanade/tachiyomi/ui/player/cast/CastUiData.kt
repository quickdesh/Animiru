package eu.kanade.tachiyomi.ui.player.cast

import androidx.compose.runtime.Stable
import dev.vivvvek.seeker.Segment
import tachiyomi.cast.domain.TrackInformation

@Stable
data class CastUiData(
    val isLoadingEpisode: Boolean = false,
    val duration: Long = 0L,
    val skipIntroLength: Long = 0L,
    val isSeeking: Boolean = false,
    val seekPosition: Float = 0f,
    val skipIntroText: String? = null,
    val netflixTimeout: Int? = null,
    val invertDurationTimer: Boolean = false,
    val subTracks: List<TrackInformation> = emptyList(),
    val audioTracks: List<TrackInformation> = emptyList(),
    val currentAudioId: Long = -1L,
    val currentSubId: Long = -1L,
    val chapters: List<Segment> = emptyList(),
    val currentChapter: Segment? = null,
    val showChapterIndicator: Boolean = false,
    val sheetShown: CastSheet = CastSheet.None,
    val dialogShown: CastDialog = CastDialog.None,
)

enum class CastSheet {
    Quality,
    Audio,
    Subtitle,
    Chapter,
    PlaybackSpeed,
    Playlist,
    None,
}

enum class CastDialog {
    IntroLength,
    None,
}
