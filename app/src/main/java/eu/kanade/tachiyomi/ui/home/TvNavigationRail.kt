package eu.kanade.tachiyomi.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface as TVSurface
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.recents.RecentsTab
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tachiyomi.domain.library.service.LibraryPreferences
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun TvNavigationRail(
    tabs: List<Tab>,
    modifier: Modifier = Modifier,
) {
    val tabNavigator = LocalTabNavigator.current
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()

    Surface(
        tonalElevation = 1.4.dp,
    ) {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .width(40.dp)
                .padding(vertical = 8.dp)
                .focusGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEach { tab ->
                val selected = tabNavigator.current::class == tab::class
                val interactionSource = remember { MutableInteractionSource() }
                val title = tab.options.title

                TVSurface(
                    onClick = {
                        if (!selected) {
                            tabNavigator.current = tab
                        } else {
                            scope.launch { tab.onReselect(navigator) }
                        }
                    },
                    onLongClick = {
                        if (selected) scope.launch { tab.onReselectHold(navigator) }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .semantics {
                            this.selected = selected
                            contentDescription = title
                        },
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        focusedContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = ClickableSurfaceDefaults.shape(),
                    scale = ClickableSurfaceDefaults.scale(
                        scale = 1f,
                        focusedScale = 1.2f
                    ),
                    interactionSource = interactionSource,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = if (selected) 4.dp else 0.dp,
                                color = if (selected)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedIcon(tab)
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgedIcon(tab: Tab) {
    BadgedBox(
        badge = {
            when {
                RecentsTab::class.isInstance(tab) -> {
                    val count by produceState(initialValue = 0) {
                        val pref = Injekt.get<LibraryPreferences>()
                        pref.newUpdatesCount().changes()
                            .collectLatest { value = it }
                    }
                    if (count > 0) {
                        Badge { Text(count.toString()) }
                    }
                }

                BrowseTab::class.isInstance(tab) -> {
                    val count by produceState(initialValue = 0) {
                        val pref = Injekt.get<SourcePreferences>()
                        pref.extensionUpdatesCount().changes()
                            .collectLatest { value = it }
                    }
                    if (count > 0) {
                        Badge { Text(count.toString()) }
                    }
                }
            }
        }
    ) {
        Icon(
            painter = tab.options.icon!!,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(32.dp)
        )
    }
}
