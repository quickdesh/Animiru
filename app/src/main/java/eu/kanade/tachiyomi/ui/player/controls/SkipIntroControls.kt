package eu.kanade.tachiyomi.ui.player.controls

import androidx.compose.runtime.Composable
import eu.kanade.tachiyomi.ui.player.controls.components.FilledControlsButton
import tachiyomi.domain.custombutton.model.CustomButton

@Composable
fun SkipIntroControls(
    customButton: CustomButton?,
    customButtonTitle: String,
    skipIntroButton: String?,
    onPressSkipIntroButton: () -> Unit,
    onCustomButtonClick: () -> Unit,
    onCustomButtonLongClick: () -> Unit,
) {
    if (skipIntroButton != null) {
        FilledControlsButton(
            text = skipIntroButton,
            onClick = onPressSkipIntroButton,
            onLongClick = {},
        )
    } else if (customButton != null) {
        FilledControlsButton(
            text = customButtonTitle,
            onClick = onCustomButtonClick,
            onLongClick = onCustomButtonLongClick,
        )
    }
}
