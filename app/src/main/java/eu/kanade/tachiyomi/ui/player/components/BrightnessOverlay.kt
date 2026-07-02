package eu.kanade.tachiyomi.ui.player.components

import androidx.activity.compose.LocalActivity
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.tachiyomi.ui.player.settings.PlayerPreferences
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.math.abs

@Composable
fun BrightnessOverlay(
    @FloatRange(from = -0.75, to = 1.0) brightness: Float,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.currentOrThrow
    val playerPreferences = remember { Injekt.get<PlayerPreferences>() }

    LaunchedEffect(Unit) {
        if (brightness < 0f) {
            activity.window.attributes = activity.window.attributes.apply {
                screenBrightness = 0f
            }
        }
    }

    LaunchedEffect(brightness) {
        if (brightness < 0f) return@LaunchedEffect

        activity.window.attributes = activity.window.attributes.apply {
            screenBrightness = brightness.coerceIn(0f, 1f)
        }
    }

    DisposableEffect(brightness) {
        onDispose {
            if (playerPreferences.rememberPlayerBrightness.get() && brightness != -1f) {
                playerPreferences.playerBrightnessValue.set(brightness)
            }
        }
    }

    if (brightness < 0) {
        val brightnessAlpha = remember(brightness) {
            abs(brightness)
        }

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = brightnessAlpha
                },
        ) {
            drawRect(Color.Black)
        }
    }
}
