package eu.kanade.tachiyomi.ui.anime.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.anime.AnimeNotesScreen
import eu.kanade.presentation.util.Screen
import kotlinx.coroutines.flow.update
import mihon.core.viewmodel.StateViewModel
import tachiyomi.core.common.util.lang.launchNonCancellable
import tachiyomi.domain.anime.interactor.UpdateAnimeNotes
import tachiyomi.domain.anime.model.Anime
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnimeNotesScreen(
    private val anime: Anime,
) : Screen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModel = viewModel<Model>(
            factory = Model.Factory,
            extras = CreationExtras {
                set(Model.ANIME_KEY, anime)
            },
        )
        val state by viewModel.state.collectAsState()

        AnimeNotesScreen(
            state = state,
            navigateUp = navigator::pop,
            onUpdate = viewModel::updateNotes,
        )
    }

    class Model(
        private val anime: Anime,
        private val updateAnimeNotes: UpdateAnimeNotes = Injekt.get(),
    ) : StateViewModel<State>(State(anime, anime.notes)) {

        companion object {
            val ANIME_KEY = CreationExtras.Key<Anime>()

            val Factory = viewModelFactory {
                initializer {
                    Model(
                        anime = get(ANIME_KEY)!!,
                    )
                }
            }
        }

        fun updateNotes(content: String) {
            if (content == state.value.notes) return

            mutableState.update {
                it.copy(notes = content)
            }

            viewModelScope.launchNonCancellable {
                updateAnimeNotes(anime.id, content)
            }
        }
    }

    @Immutable
    data class State(
        val anime: Anime,
        val notes: String,
    )
}
