// AM (REMOVE_TABBED_SCREENS) -->
// AM (STORAGE_SCREEN) -->
package eu.kanade.tachiyomi.ui.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.storage.StorageScreenContent
import eu.kanade.presentation.more.storage.StorageScreenState
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.anime.AnimeScreen
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.LoadingScreen

class StorageScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModel = viewModel<StorageViewModel>()
        val state by viewModel.state.collectAsState()
        val selectedCategory by viewModel.selectedCategory.collectAsState()

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(AMMR.strings.pref_storage_overview),
                    navigateUp = navigator::pop,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { paddingValues ->
            if (state is StorageScreenState.Loading) {
                LoadingScreen(
                    percentage = (state as StorageScreenState.Loading).progress,
                    message = AMMR.strings.calculating_storage_overview,
                )
                return@Scaffold
            }

            StorageScreenContent(
                state = state as StorageScreenState.Success,
                selectedCategory = selectedCategory,
                paddingValues = paddingValues,
                onCategorySelected = viewModel::setSelectedCategory,
                onDelete = viewModel::deleteAnime,
                onClickCover = { item -> navigator.push(AnimeScreen(item.anime.id)) },
            )
        }
    }
}
// <-- AM (STORAGE_SCREEN)
// <-- AM (REMOVE_TABBED_SCREENS)
