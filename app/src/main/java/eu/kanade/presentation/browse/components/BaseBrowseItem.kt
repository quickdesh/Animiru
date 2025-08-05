package eu.kanade.presentation.browse.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import tachiyomi.presentation.core.components.material.padding

@Composable
fun BaseBrowseItem(
    modifier: Modifier = Modifier,
    onClickItem: () -> Unit = {},
    onLongClickItem: () -> Unit = {},
    // AM (BROWSE) -->
    pin: @Composable (RowScope.() -> Unit)? = null,
    // <-- AM (BROWSE)
    icon: @Composable RowScope.() -> Unit = {},
    action: @Composable RowScope.() -> Unit = {},
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .combinedClickable(
                onClick = onClickItem,
                onLongClick = onLongClickItem,
            )
            .padding(
                // AM (BROWSE) -->
                start = if (pin != null) MaterialTheme.padding.none else MaterialTheme.padding.medium,
                end = MaterialTheme.padding.medium,
                top = MaterialTheme.padding.small,
                bottom = MaterialTheme.padding.small,
                // <-- AM (BROWSE)
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // AM (BROWSE) -->
        if (pin != null) pin()
        // <-- AM (BROWSE)
        icon()
        content()
        action()
    }
}
