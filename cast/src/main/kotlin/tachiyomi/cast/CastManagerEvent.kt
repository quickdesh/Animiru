package tachiyomi.cast

sealed interface CastManagerEvent {
    data class Next(val next: Boolean) : CastManagerEvent
    data object PlayPause : CastManagerEvent
}
