package eu.kanade.tachiyomi.ui.player.cast.components

import androidx.compose.runtime.Composable
import eu.kanade.presentation.more.settings.screen.player.PlayerSettingsGesturesScreen.SkipIntroLengthDialog
import eu.kanade.tachiyomi.ui.player.cast.CastDialog
import eu.kanade.tachiyomi.ui.player.cast.CastUiData

@Composable
fun CastDialogs(
    dialogShown: CastDialog,
    castUiData: CastUiData,

    // Skip intro
    onSkipIntroLengthChange: (Long) -> Unit,

    onDismissRequest: () -> Unit,
) {
    when (dialogShown) {
        CastDialog.None -> { }
        CastDialog.IntroLength -> {
            SkipIntroLengthDialog(
                initialSkipIntroLength = castUiData.skipIntroLength.toInt(),
                onDismissRequest = onDismissRequest,
                onValueChanged = {
                    onSkipIntroLengthChange(it.toLong())
                    onDismissRequest()
                },
            )
        }
    }
}
