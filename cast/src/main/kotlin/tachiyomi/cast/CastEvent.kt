package tachiyomi.cast

sealed interface CastEvent {
    data object Connected : CastEvent
    data class PlaybackError(val exception: Exception) : CastEvent
    data class Disconnected(val lastPosition: Long) : CastEvent
}
