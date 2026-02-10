package tachiyomi.presentation.core.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState

fun LazyListState.shouldExpandFAB(): Boolean = lastScrolledBackward || !canScrollForward || !canScrollBackward

// AY -->
fun LazyGridState.shouldExpandFAB(): Boolean = lastScrolledBackward || !canScrollForward || !canScrollBackward
// <-- AY
