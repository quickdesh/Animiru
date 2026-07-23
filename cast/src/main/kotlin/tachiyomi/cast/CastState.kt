package tachiyomi.cast

import androidx.compose.runtime.Stable

@Stable
data class CastState(
    val isConnected: Boolean = false,
    val deviceName: String? = null,
    val playing: Boolean = false,
    val buffering: Boolean = true,
    val position: Long = 0L,
    val durationMs: Long = 0L,
    val hasLoadedVideo: Boolean = false,
    val isLoading: Boolean = true,
    val lastLoadedSubId: Long = -1L,
    val lastLoadedAudioId: Long = -1L,

    val volume: Double = 1.0,
    val muted: Boolean = false,
    val speed: Double = 1.0,
)
