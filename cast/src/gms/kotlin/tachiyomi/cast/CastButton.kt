package tachiyomi.cast

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.mediarouter.app.MediaRouteChooserDialog
import com.google.android.gms.cast.framework.CastContext
import tachiyomi.presentation.core.components.material.padding

@Composable
fun CastButton(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = MaterialTheme.padding.medium,
    verticalSpacing: Dp = MaterialTheme.padding.medium,
    iconSize: Dp = 20.dp,
) {
    val context = LocalContext.current

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .combinedClickable(
                enabled = true,
                onClick = {
                    val selector = CastContext.getSharedInstance(context)
                        .mergedSelector ?: return@combinedClickable

                    MediaRouteChooserDialog(context).apply {
                        routeSelector = selector
                        show()
                    }
                },
                interactionSource = interactionSource,
                indication = null,
            )
            .clip(CircleShape)
            .indication(
                interactionSource,
                ripple(),
            )
            .padding(
                vertical = verticalSpacing,
                horizontal = horizontalSpacing,
            ),
    ) {
        Icon(
            imageVector = Icons.Default.Cast,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
        )
    }
}
