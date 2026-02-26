package eu.kanade.presentation.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import eu.kanade.presentation.anime.DownloadAction
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun DownloadDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onDownloadClicked: (DownloadAction) -> Unit,
    offset: DpOffset? = null,
) {
    if (offset != null) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            offset = offset,
            content = {
                DownloadDropdownMenuItems(
                    onDismissRequest = onDismissRequest,
                    onDownloadClicked = onDownloadClicked,
                )
            },
        )
    } else {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            content = {
                DownloadDropdownMenuItems(
                    onDismissRequest = onDismissRequest,
                    onDownloadClicked = onDownloadClicked,
                )
            },
        )
    }
}

@Composable
private fun DownloadDropdownMenuItems(
    onDismissRequest: () -> Unit,
    onDownloadClicked: (DownloadAction) -> Unit,
) {
    val options = persistentListOf(
        DownloadAction.NEXT_1_EPISODE to pluralStringResource(AYMR.plurals.download_amount_anime, 1, 1),
        DownloadAction.NEXT_5_EPISODES to pluralStringResource(AYMR.plurals.download_amount_anime, 5, 5),
        DownloadAction.NEXT_10_EPISODES to pluralStringResource(AYMR.plurals.download_amount_anime, 10, 10),
        DownloadAction.NEXT_25_EPISODES to pluralStringResource(AYMR.plurals.download_amount_anime, 25, 25),
        DownloadAction.UNSEEN_EPISODES to stringResource(AYMR.strings.download_unseen),
        DownloadAction.BOOKMARKED_EPISODES to stringResource(MR.strings.download_bookmarked),
    )

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
