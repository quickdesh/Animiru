package eu.kanade.tachiyomi.ui.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.NavigationDrawerItemDefaults
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import cafe.adriel.voyager.navigator.tab.TabNavigator
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.recents.RecentsTab
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.domain.library.service.LibraryPreferences
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun NavigationDrawerScope.NavigationDrawerContent(
    tabs: List<Tab>,
    tabNavigator: TabNavigator,
    drawerState: DrawerState,
) {

    val drawerIsOpen = drawerState.currentValue == DrawerValue.Open
    var initialFocusComplete by remember { mutableStateOf(false) }

    val focusRequesters = remember(tabs) {
        tabs.associateWith { FocusRequester() }
    }

    LaunchedEffect(drawerState.currentValue) {
        initialFocusComplete = false
        if (drawerIsOpen) {
            focusRequesters[tabNavigator.current]?.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .sizeIn(maxWidth = 280.dp)
            .padding(vertical = 24.dp)
            .focusProperties {
                canFocus = !initialFocusComplete
            }
            .focusGroup()
    ) {

        tabs.forEach { tab ->
            val selected = tabNavigator.current::class == tab::class

            val recentsCount by produceState(initialValue = 0) {
                if (RecentsTab::class.isInstance(tab)) {
                    val pref = Injekt.get<LibraryPreferences>()
                    pref.newUpdatesCount().changes()
                        .collectLatest { value = it }
                }
            }

            val browseCount by produceState(initialValue = 0) {
                if (BrowseTab::class.isInstance(tab)) {
                    val pref = Injekt.get<SourcePreferences>()
                    pref.extensionUpdatesCount().changes()
                        .collectLatest { value = it }
                }
            }

            val badgeCount = when {
                RecentsTab::class.isInstance(tab) -> recentsCount
                BrowseTab::class.isInstance(tab) -> browseCount
                else -> 0
            }

            val canFocusThis = drawerIsOpen && (initialFocusComplete || selected)

            NavigationDrawerItem(
                selected = selected,
                onClick = {
                    // TODO: Replace with reselect tab option || tabNavigator.current = tab
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .focusRequester(focusRequesters[tab]!!)
                    .focusProperties {
                        canFocus = canFocusThis
                    }
                    .onFocusChanged {
                        if (it.isFocused && drawerIsOpen) {
                            if (!initialFocusComplete) {
                                initialFocusComplete = true
                            }
                            tabNavigator.current = tab
                        }
                    },
                leadingContent = {
                    Icon(
                        painter = tab.options.icon!!,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                },
                trailingContent = if (badgeCount > 0) {
                    {
                        Badge {
                            Text(badgeCount.toString())
                        }
                    }
                } else null,
                content = {
                    Text(tab.options.title)
                },
                colors = NavigationDrawerItemDefaults.colors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                    focusedContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    }
}
