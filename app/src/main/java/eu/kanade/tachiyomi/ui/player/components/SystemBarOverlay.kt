package eu.kanade.tachiyomi.ui.player.components

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import cafe.adriel.voyager.navigator.currentOrThrow

// From https://github.com/MakD/AFinity/blob/master/app/src/main/java/com/makd/afinity/ui/player/utils/PlayerSystemBarsController.kt
@SuppressLint("WrongConstant")
@Composable
fun SystemBarOverlay(showStatusBar: Boolean) {
    val activity = LocalActivity.currentOrThrow

    LaunchedEffect(showStatusBar) {
        val window = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        if (showStatusBar) {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        } else {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val window = activity.window
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)

            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }
}
