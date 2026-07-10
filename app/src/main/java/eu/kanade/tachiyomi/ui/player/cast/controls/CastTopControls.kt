package eu.kanade.tachiyomi.ui.player.cast.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.tachiyomi.ui.player.cast.components.CastControlButton
import tachiyomi.presentation.core.components.material.padding

@Composable
fun CastTopControls(
    deviceName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        CastControlButton(
            icon = Icons.AutoMirrored.Default.ArrowBack,
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 60.dp),
        ) {
            Icon(
                Icons.Default.CastConnected,
                null,
                tint = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = deviceName,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
@Preview
private fun CastTopControlsPreview() {
    CastTopControls(
        deviceName = "GM1911-180",
        onBack = { },
    )
}
