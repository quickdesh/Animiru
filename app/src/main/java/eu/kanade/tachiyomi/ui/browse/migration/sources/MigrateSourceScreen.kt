// AM (BROWSE) -->
package eu.kanade.tachiyomi.ui.browse.migration.sources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.MigrateSourceScreen
import eu.kanade.tachiyomi.ui.browse.migration.anime.MigrateAnimeScreen

class MigrateSourceScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = viewModel<MigrateSourceViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        MigrateSourceScreen(
            state = state,
            navigateUp = navigator::pop,
            onClickItem = { source -> navigator.push(MigrateAnimeScreen(source.id)) },
            onToggleSortingDirection = viewModel::toggleSortingDirection,
            onToggleSortingMode = viewModel::toggleSortingMode,
        )
    }
}
// <-- AM (BROWSE)
