package eu.kanade.presentation.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapCalls
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.source.model.installedExtension
import eu.kanade.presentation.browse.components.BaseSourceItem
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarTitle
import eu.kanade.tachiyomi.ui.browse.extension.details.ExtensionDetailsScreen
import eu.kanade.tachiyomi.ui.browse.migration.sources.MigrateSourceScreen
import eu.kanade.tachiyomi.ui.browse.source.SourcesFilterScreen
import eu.kanade.tachiyomi.ui.browse.source.SourcesScreenModel
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreenModel.Listing
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.util.system.LocaleHelper
import tachiyomi.domain.source.model.Pin
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.SECONDARY_ALPHA
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.theme.header
import tachiyomi.presentation.core.util.plus
import tachiyomi.presentation.core.util.shouldExpandFAB
import tachiyomi.source.local.LocalSource
import tachiyomi.source.local.isLocal

@Composable
fun SourcesScreen(
    state: SourcesScreenModel.State,
    onClickItem: (Source, Listing) -> Unit,
    onClickPin: (Source) -> Unit,
    onLongClickItem: (Source) -> Unit,
    // AM (BROWSE) -->
    toExtensionsScreen: () -> Unit,
    updateCount: Int,
    modifier: Modifier = Modifier,
    // <-- AM (BROWSE)
) {
    // AM (BROWSE) -->
    val navigator = LocalNavigator.currentOrThrow
    Scaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            AppBar(
                titleContent = { AppBarTitle(stringResource(MR.strings.browse)) },
                actions = {
                    IconButton(onClick = { navigator.push(GlobalSearchScreen()) }) {
                        Icon(
                            Icons.Outlined.TravelExplore,
                            contentDescription = stringResource(MR.strings.action_global_search),
                        )
                    }
                    IconButton(onClick = { navigator.push(SourcesFilterScreen()) }) {
                        Icon(
                            Icons.Outlined.FilterList,
                            contentDescription = stringResource(MR.strings.action_filter),
                        )
                    }
                    IconButton(onClick = { navigator.push(MigrateSourceScreen()) }) {
                        Icon(
                            Icons.Outlined.SwapCalls,
                            contentDescription = stringResource(MR.strings.action_migrate),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        val extensionsListState = rememberLazyListState()
        // <-- AM (BROWSE)
        when {
            state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
            state.isEmpty -> EmptyScreen(
                stringRes = MR.strings.source_empty_screen,
                modifier = Modifier.padding(contentPadding),
            )
            else -> {
                // AM (BROWSE) -->
                Scaffold(
                    floatingActionButton = {
                        val buttonText = if (updateCount != 0) MR.strings.ext_update else MR.strings.ext_install
                        val buttonIcon = if (updateCount != 0) Icons.Filled.Upload else Icons.Filled.Download
                        ExtendedFloatingActionButton(
                            text = { Text(text = stringResource(buttonText)) },
                            icon = { Icon(imageVector = buttonIcon, contentDescription = null) },
                            onClick = { toExtensionsScreen() },
                            expanded = (extensionsListState.shouldExpandFAB()) || updateCount != 0,
                        )
                    },
                ) {
                    ScrollbarLazyColumn(
                        state = extensionsListState,
                        // <-- AM (BROWSE)
                        contentPadding = contentPadding + topSmallPaddingValues,
                    ) {
                        items(
                            items = state.items,
                            contentType = {
                                when (it) {
                                    is SourceUiModel.Header -> "header"
                                    is SourceUiModel.Item -> "item"
                                }
                            },
                            key = {
                                when (it) {
                                    is SourceUiModel.Header -> it.hashCode()
                                    is SourceUiModel.Item -> "source-${it.source.key()}"
                                }
                            },
                        ) { model ->
                            when (model) {
                                is SourceUiModel.Header -> {
                                    SourceHeader(
                                        modifier = Modifier.animateItem(),
                                        language = model.language,
                                    )
                                }
                                is SourceUiModel.Item -> SourceItem(
                                    modifier = Modifier.animateItem(),
                                    // AM (BROWSE) -->
                                    navigator = navigator,
                                    // <-- AM (BROWSE)
                                    source = model.source,
                                    onClickItem = onClickItem,
                                    onLongClickItem = onLongClickItem,
                                    onClickPin = onClickPin,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceHeader(
    language: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Text(
        text = LocaleHelper.getSourceDisplayName(language, context),
        modifier = modifier
            .padding(horizontal = MaterialTheme.padding.medium, vertical = MaterialTheme.padding.small),
        style = MaterialTheme.typography.header,
    )
}

@Composable
private fun SourceItem(
    source: Source,
    // AM (BROWSE) -->
    navigator: Navigator,
    // <-- AM (BROWSE)
    onClickItem: (Source, Listing) -> Unit,
    onLongClickItem: (Source) -> Unit,
    onClickPin: (Source) -> Unit,
    modifier: Modifier = Modifier,
) {
    BaseSourceItem(
        modifier = modifier,
        source = source,
        onClickItem = { onClickItem(source, Listing.Popular) },
        onLongClickItem = { onLongClickItem(source) },
        action = {
            if (source.supportsLatest) {
                TextButton(
                    onClick = { onClickItem(source, Listing.Latest) },
                    // AM (BROWSE) -->
                    modifier = Modifier.takeIf { source.id == LocalSource.ID }?.padding(end = 48.dp) ?: Modifier,
                    // <-- AM (BROWSE)
                ) {
                    Text(
                        text = stringResource(MR.strings.latest),
                        style = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
            // AM (BROWSE) -->
            if (source.id != LocalSource.ID) {
                SourceSettingsButton(
                    navigator = navigator,
                    source = source,
                )
            }
        },
        pin = {
            // <-- AM (BROWSE)
            SourcePinButton(
                isPinned = Pin.Pinned in source.pin,
                onClick = { onClickPin(source) },
            )
        }
    )
}

@Composable
private fun SourcePinButton(
    isPinned: Boolean,
    onClick: () -> Unit,
) {
    val icon = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin
    val tint = if (isPinned) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onBackground.copy(
            alpha = SECONDARY_ALPHA,
        )
    }
    val description = if (isPinned) MR.strings.action_unpin else MR.strings.action_pin
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            tint = tint,
            // AM (BROWSE) -->
            modifier = Modifier
                .size(16.dp)
                .rotate(-30f),
            // <-- AM (BROWSE)
            contentDescription = stringResource(description),
        )
    }
}


// AM (BROWSE) -->
@Composable
private fun SourceSettingsButton(
    navigator: Navigator,
    source: Source,
) {
    val extension = source.installedExtension ?: return
    IconButton(onClick = { navigator.push(ExtensionDetailsScreen(extension.pkgName)) }) {
        Icon(
            imageVector = Icons.Outlined.Settings,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = stringResource(MR.strings.label_settings),
        )
    }
}
// <-- AM (BROWSE)

@Composable
fun SourceOptionsDialog(
    source: Source,
    onClickPin: () -> Unit,
    onClickDisable: () -> Unit,
    // AM (BROWSE) -->
    onClickUninstall: () -> Unit,
    // <-- AM (BROWSE)
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = source.visualName)
        },
        text = {
            Column {
                val textId = if (Pin.Pinned in source.pin) MR.strings.action_unpin else MR.strings.action_pin
                Text(
                    text = stringResource(textId),
                    modifier = Modifier
                        .clickable(onClick = onClickPin)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
                if (!source.isLocal()) {
                    Text(
                        text = stringResource(MR.strings.action_disable),
                        modifier = Modifier
                            .clickable(onClick = onClickDisable)
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    )
                }
                // AM (BROWSE) -->
                Text(
                    text = stringResource(resource = MR.strings.ext_uninstall),
                    modifier = Modifier
                        .clickable(onClick = onClickUninstall)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
                // <-- AM (BROWSE)
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {},
    )
}

sealed interface SourceUiModel {
    data class Item(val source: Source) : SourceUiModel
    data class Header(val language: String) : SourceUiModel
}
