package eu.kanade.tachiyomi.ui.player.controls.components.panels

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.tachiyomi.ui.player.controls.components.panels.components.MultiCardPanel
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun VideoSettingsPanel(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MultiCardPanel(
        onDismissRequest = onDismissRequest,
        title = stringResource(AMMR.strings.player_sheets_video_settings_title),
        cardCount = 2,
        modifier = modifier,
    ) { index, cardModifier ->
        when (index) {
            0 -> VideoSettingsDebandCard(cardModifier)
            1 -> VideoSettingsFiltersCard(cardModifier)
            else -> {}
        }
    }
}
