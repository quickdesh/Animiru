package eu.kanade.tachiyomi.ui.browse

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import eu.kanade.presentation.browse.ExtensionScreen
import eu.kanade.presentation.browse.SourceOptionsDialog
import eu.kanade.presentation.browse.SourcesScreen
import eu.kanade.presentation.util.Tab
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connection.discord.DiscordRPCService
import eu.kanade.tachiyomi.data.connection.discord.DiscordScreen
import eu.kanade.tachiyomi.extension.model.Extension
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionsScreenModel
import eu.kanade.tachiyomi.ui.browse.extension.details.ExtensionDetailsScreen
import eu.kanade.tachiyomi.ui.browse.migration.sources.MigrateSourceScreen
import eu.kanade.tachiyomi.ui.browse.source.SourcesScreenModel
import eu.kanade.tachiyomi.ui.browse.source.browse.BrowseSourceScreen
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.GlobalSearchScreen
import eu.kanade.tachiyomi.ui.home.HomeScreen
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.ui.webview.WebViewScreen
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import kotlin.math.abs

// AM (BROWSE)  -->
internal var goToExtensions = false
// <-- AM (BROWSE)

data object BrowseTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val isSelected = LocalTabNavigator.current.current.key == key
            val image = AnimatedImageVector.animatedVectorResource(R.drawable.anim_browse_enter)
            return TabOptions(
                // AM (BROWSE)  -->
                index = 2u,
                // <-- AM (BROWSE)
                title = stringResource(MR.strings.browse),
                icon = rememberAnimatedVectorPainter(image, isSelected),
            )
        }

    override suspend fun onReselect(navigator: Navigator) {
        navigator.push(GlobalSearchScreen())
    }

    // AM (TAB_HOLD) -->
    override suspend fun onReselectHold(navigator: Navigator) {
        navigator.push(MigrateSourceScreen())
    }
    // <-- AM (TAB_HOLD)

    private val switchToExtensionTabChannel = Channel<Unit>(1, BufferOverflow.DROP_OLDEST)

    fun showExtension() {
        switchToExtensionTabChannel.trySend(Unit)
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current

        // AM (BROWSE) -->
        val snackbarHostState = SnackbarHostState()
        val navigator = LocalNavigator.currentOrThrow
        val sourcesScreenModel = rememberScreenModel { SourcesScreenModel() }
        val sourcesState by sourcesScreenModel.state.collectAsState()
        val updateCount by sourcesScreenModel.sourcePreferences.extensionUpdatesCount.collectAsState()

        val extensionScreenModel = rememberScreenModel { ExtensionsScreenModel() }
        val extensionsState by extensionScreenModel.state.collectAsState()

        var inExtensionsScreen by remember { mutableStateOf(goToExtensions) }
        val animationDuration = 300
        val animationEasing = FastOutSlowInEasing

        BackHandler(enabled = inExtensionsScreen) { inExtensionsScreen = false }

        val alpha by animateFloatAsState(
            targetValue = if (!inExtensionsScreen) 1f else -1f,
            animationSpec = tween(durationMillis = animationDuration),
        )

        Box {
            AnimatedVisibility(
                visible = !inExtensionsScreen,
                modifier = Modifier.fillMaxSize(),
                enter = slideInHorizontally(
                    initialOffsetX = { -200 },
                    animationSpec = tween(durationMillis = animationDuration, easing = animationEasing),
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { -200 },
                    animationSpec = tween(durationMillis = animationDuration, easing = animationEasing),
                ),
            ) {
                goToExtensions = false
                SourcesScreen(
                    state = sourcesState,
                    onClickItem = { source, listing ->
                        navigator.push(BrowseSourceScreen(source.id, listing.query))
                    },
                    onClickPin = sourcesScreenModel::togglePin,
                    onLongClickItem = sourcesScreenModel::showSourceDialog,
                    toExtensionsScreen = { inExtensionsScreen = true },
                    updateCount = updateCount,
                    modifier = Modifier.alpha(alpha.coerceAtLeast(0f)),
                )
            }

            AnimatedVisibility(
                visible = inExtensionsScreen,
                modifier = Modifier.fillMaxSize(),
                enter = slideInHorizontally(
                    initialOffsetX = { 200 },
                    animationSpec = tween(durationMillis = animationDuration, easing = animationEasing),
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { 200 },
                    animationSpec = tween(durationMillis = animationDuration, easing = animationEasing),
                ),
            ) {
                goToExtensions = true
                ExtensionScreen(
                    state = extensionsState,
                    searchQuery = extensionsState.searchQuery,

                    onLongClickItem = { extension ->
                        when (extension) {
                            is Extension.Available -> extensionScreenModel.installExtension(extension)
                            else -> extensionScreenModel.uninstallExtension(extension)
                        }
                    },
                    onClickItemCancel = extensionScreenModel::cancelInstallUpdateExtension,
                    onClickUpdateAll = extensionScreenModel::updateAllExtensions,
                    onOpenWebView = { extension ->
                        extension.sources.getOrNull(0)?.let {
                            navigator.push(WebViewScreen(url = it.baseUrl, initialTitle = it.name, sourceId = it.id))
                        }
                    },
                    onInstallExtension = extensionScreenModel::installExtension,
                    onOpenExtension = { navigator.push(ExtensionDetailsScreen(it.pkgName)) },
                    onTrustExtension = extensionScreenModel::trustExtension,
                    onUninstallExtension = extensionScreenModel::uninstallExtension,
                    onUpdateExtension = extensionScreenModel::updateExtension,
                    onRefresh = extensionScreenModel::findAvailableExtensions,
                    toSourcesScreen = { inExtensionsScreen = false },
                    onChangeSearchQuery = extensionScreenModel::search,
                    modifier = Modifier.alpha(abs(alpha.coerceAtMost(0f))),
                )
            }
        }

        sourcesState.dialog?.let { dialog ->
            val source = dialog.source
            SourceOptionsDialog(
                source = source,
                onClickPin = {
                    sourcesScreenModel.togglePin(source)
                    sourcesScreenModel.closeDialog()
                },
                onClickDisable = {
                    sourcesScreenModel.toggleSource(source)
                    sourcesScreenModel.closeDialog()
                },
                onClickUninstall = {
                    val ext = dialog.extension ?: return@SourceOptionsDialog
                    sourcesScreenModel.uninstallExtension(ext)
                    sourcesScreenModel.closeDialog()
                },
                onDismiss = sourcesScreenModel::closeDialog,
            )
        }

        LaunchedEffect(Unit) {
            // AM (DISCORD_RPC) -->
            with(DiscordRPCService) {
                discordScope.launchIO { setScreen(context.applicationContext, DiscordScreen.BROWSE) }
            }
            // <-- AM (DISCORD_RPC)
            (context as? MainActivity)?.ready = true
        }

        LaunchedEffect(inExtensionsScreen) {
            HomeScreen.showBottomNav(!inExtensionsScreen)
        }
        // <-- AM (BROWSE)
    }
}
