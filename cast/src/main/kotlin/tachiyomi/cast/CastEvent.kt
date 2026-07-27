package tachiyomi.cast

sealed interface CastEvent {
    data object ConnectionError : CastEvent
    data object ConnectionStart : CastEvent
    data object Connected : CastEvent
    data class NextEpisode(val next: Boolean) : CastEvent
    data class OnSecondReached(val position: Int) : CastEvent
    data class PlaybackError(val exception: Exception) : CastEvent
    data class Disconnected(val lastPosition: Long) : CastEvent
    data class TrackLoadResult(val trackId: Long, val success: Boolean, val isAudio: Boolean) : CastEvent
    data object LoadingFailed : CastEvent
    data object Ready : CastEvent
}
