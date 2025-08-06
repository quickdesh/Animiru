// AM (BROWSE) -->
package eu.kanade.tachiyomi.ui.browse.migration.sources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.MigrateSourceScreen
import eu.kanade.tachiyomi.ui.browse.migration.anime.MigrateAnimeScreen

class MigrateSourceScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { MigrateSourceScreenModel() }
        val state by screenModel.state.collectAsState()

        MigrateSourceScreen(
            state = state,
            navigateUp = navigator::pop,
            onClickItem = { source -> navigator.push(MigrateAnimeScreen(source.id)) },
            onToggleSortingDirection = screenModel::toggleSortingDirection,
            onToggleSortingMode = screenModel::toggleSortingMode,
        )
    }
}
// <-- AM (BROWSE)
