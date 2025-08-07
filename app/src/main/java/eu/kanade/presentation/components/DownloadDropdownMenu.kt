package eu.kanade.presentation.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.presentation.anime.DownloadAction
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun DownloadDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onDownloadClicked: (DownloadAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = persistentListOf(
        DownloadAction.NEXT_1_EPISODE to pluralStringResource(MR.plurals.download_amount, 1, 1),
        DownloadAction.NEXT_5_EPISODES to pluralStringResource(MR.plurals.download_amount, 5, 5),
        DownloadAction.NEXT_10_EPISODES to pluralStringResource(MR.plurals.download_amount, 10, 10),
        DownloadAction.NEXT_25_EPISODES to pluralStringResource(MR.plurals.download_amount, 25, 25),
        DownloadAction.UNSEEN_EPISODES to stringResource(AYMR.strings.download_unseen),
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        options.map { (downloadAction, string) ->
            DropdownMenuItem(
                text = { Text(text = string) },
                onClick = {
                    onDownloadClicked(downloadAction)
                    onDismissRequest()
                },
            )
        }
    }
}
