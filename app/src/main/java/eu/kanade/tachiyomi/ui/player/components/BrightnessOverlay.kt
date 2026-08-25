package eu.kanade.tachiyomi.ui.player.components

import android.view.WindowManager
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
import animiru.domain.player.service.PlayerPreferences
import cafe.adriel.voyager.navigator.currentOrThrow
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.math.abs

@Composable
fun BrightnessOverlay(
    @FloatRange(from = -1.0, to = 1.0) brightness: Float,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.currentOrThrow
    val playerPreferences = remember { Injekt.get<PlayerPreferences>() }

    LaunchedEffect(Unit) {
        if (brightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            activity.window.attributes = activity.window.attributes.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        } else if (brightness < 0f) {
            activity.window.attributes = activity.window.attributes.apply {
                screenBrightness = 0f
            }
        }
    }

    LaunchedEffect(brightness) {
        if (brightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            activity.window.attributes = activity.window.attributes.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
            return@LaunchedEffect
        }

        activity.window.attributes = activity.window.attributes.apply {
            screenBrightness = brightness.coerceIn(0f, MAX_BRIGHTNESS)
        }
    }

    DisposableEffect(brightness) {
        onDispose {
            if (playerPreferences.rememberPlayerBrightness.get() &&
                brightness != WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            ) {
                playerPreferences.playerBrightnessValue.set(brightness)
            }
        }
    }

    if (brightness in MIN_BRIGHTNESS..0f) {
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

const val MIN_BRIGHTNESS = -0.75f
const val MAX_BRIGHTNESS = 1f
