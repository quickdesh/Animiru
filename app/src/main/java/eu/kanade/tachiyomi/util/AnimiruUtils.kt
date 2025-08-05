// AM (CUSTOM_INFORMATION) -->
package eu.kanade.tachiyomi.util

import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.roundToInt

fun String.trimOrNull() = trim().nullIfBlank()

fun String.nullIfBlank(): String? = ifBlank { null }

fun <C : Collection<R>, R> C.nullIfEmpty() = ifEmpty { null }

fun Collection<String>.dropBlank() = filter { it.isNotBlank() }

/**
 * Returns the color for the given attribute.
 *
 * @param resource the attribute.
 * @param alphaFactor the alpha number [0,1].
 */
@ColorInt
fun Context.getResourceColor(@AttrRes resource: Int, alphaFactor: Float = 1f): Int {
    val typedArray = obtainStyledAttributes(intArrayOf(resource))
    val color = typedArray.getColor(0, 0)
    typedArray.recycle()

    if (alphaFactor < 1f) {
        val alpha = (color.alpha * alphaFactor).roundToInt()
        return Color.argb(alpha, color.red, color.green, color.blue)
    }

    return color
}
// <-- AM (CUSTOM_INFORMATION)
