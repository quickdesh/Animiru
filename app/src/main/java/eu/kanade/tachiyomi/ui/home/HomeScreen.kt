package eu.kanade.tachiyomi.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.rememberDrawerState
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.TabNavigator
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.anime.AnimeScreen
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.download.DownloadQueueScreen
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.recents.RecentsTab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import tachiyomi.presentation.core.components.material.Scaffold

object HomeScreen : Screen() {

    private val librarySearchEvent = Channel<String>()
    private val openTabEvent = Channel<Tab>()

    private const val TabFadeDuration = 200
    private const val TabNavigatorKey = "HomeTabs"

    private val TABS = listOf(
        LibraryTab,
        RecentsTab,
        BrowseTab,
        MoreTab,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        TabNavigator(
            tab = LibraryTab,
            key = TabNavigatorKey,
        ) { tabNavigator ->

            val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            CompositionLocalProvider(LocalNavigator provides navigator) {

                Scaffold(contentWindowInsets = WindowInsets(0)) { contentPadding ->

                    NavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            NavigationDrawerContent(
                                tabs = TABS,
                                tabNavigator = tabNavigator,
                                drawerState = drawerState
                            )
                        }
                    ) {
                        Crossfade(
                            targetState = tabNavigator.current,
                            animationSpec = tween(durationMillis = TabFadeDuration),
                            label = "tabContent",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding)
                        ) {
                            tabNavigator.saveableState(key = "currentTab", tab = it) {
                                it.Content()
                            }
                        }
                    }
                }
            }

            val drawerStateIsOpen = drawerState.currentValue == DrawerValue.Open
            BackHandler(
                enabled = drawerStateIsOpen || tabNavigator.current != LibraryTab
            ) {
                when {
                    drawerStateIsOpen -> scope.launch { drawerState.setValue(DrawerValue.Closed) }
                    tabNavigator.current != LibraryTab -> tabNavigator.current = LibraryTab
                }
            }


            LaunchedEffect(Unit) {
                launch {
                    librarySearchEvent.receiveAsFlow().collectLatest {
                        tabNavigator.current = LibraryTab
                        LibraryTab.search(query = it)
                    }
                }
                launch {
                    openTabEvent.receiveAsFlow().collectLatest {
                        tabNavigator.current = when (it) {
                            is Tab.Library -> LibraryTab
                            // AM (RECENTS) -->
                            is Tab.Recents -> {
                                if (it.toHistory) RecentsTab.showHistory()
                                RecentsTab
                            }
                            // <-- AM (RECENTS)
                            // AM (BROWSE) -->
                            is Tab.Browse -> BrowseTab
                            // <-- AM (BROWSE)
                            is Tab.More -> MoreTab
                        }

                        if (it is Tab.Library && it.animeIdToOpen != null) {
                            navigator.push(AnimeScreen(animeId = it.animeIdToOpen))
                        }
                        if (it is Tab.More && it.toDownloads) {
                            navigator.push(DownloadQueueScreen)
                        }
                    }
                }
            }
        }
    }

    suspend fun search(query: String) {
        librarySearchEvent.send(query)
    }

    suspend fun openTab(tab: Tab) {
        openTabEvent.send(tab)
    }

    sealed interface Tab {
        data class Library(val animeIdToOpen: Long? = null) : Tab

        // AM (RECENTS) -->
        data class Recents(val toHistory: Boolean) : Tab
        // <-- AM (RECENTS)

        data class Browse(val toExtensions: Boolean = false) : Tab
        data class More(val toDownloads: Boolean) : Tab
    }
}
