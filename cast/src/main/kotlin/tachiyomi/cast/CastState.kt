package tachiyomi.cast

import androidx.compose.runtime.Stable
import dev.vivvvek.seeker.Segment
import tachiyomi.cast.domain.TrackInformation

@Stable
data class CastState(
    val isConnected: Boolean = false,
    val deviceName: String? = null,
    val playing: Boolean = false,
    val loading: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val hasLoadedVideo: Boolean = false,

    val subTracks: List<TrackInformation> = emptyList(),
    val audioTracks: List<TrackInformation> = emptyList(),
    val chapters: List<Segment> = emptyList(),

    val volume: Double = 1.0,
    val muted: Boolean = false,
    val speed: Double = 1.0,
)
