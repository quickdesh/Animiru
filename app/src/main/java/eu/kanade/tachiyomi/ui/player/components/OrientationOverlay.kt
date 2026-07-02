package eu.kanade.tachiyomi.ui.player.components

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.navigator.currentOrThrow

@Composable
fun OrientationOverlay(orientation: Int?) {
    val activity = LocalActivity.currentOrThrow

    LaunchedEffect(orientation) {
        if (orientation != null) {
            activity.requestedOrientation = orientation
        }
    }
}
