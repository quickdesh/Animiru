// AM (SYNC) -->
package eu.kanade.presentation.more.settings.screen.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.domain.connection.SyncPreferences
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.connection.syncmiru.models.SyncTriggerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import tachiyomi.i18n.MR
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.presentation.core.components.LabeledCheckbox
import tachiyomi.presentation.core.components.LazyColumnWithAction
import tachiyomi.presentation.core.components.SectionCard
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

class SyncTriggerOptionsScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = metroViewModel<SyncOptionsViewModel>()
        val state by viewModel.state.collectAsState()

        Scaffold(
            topBar = {
                AppBar(
                    title = stringResource(AMMR.strings.pref_sync_options),
                    navigateUp = navigator::pop,
                    scrollBehavior = it,
                )
            },
        ) { contentPadding ->
            LazyColumnWithAction(
                contentPadding = contentPadding,
                actionLabel = stringResource(MR.strings.action_save),
                actionEnabled = true,
                onClickAction = {
                    navigator.pop()
                },
            ) {
                item {
                    SectionCard(AMMR.strings.label_triggers) {
                        Options(SyncTriggerOptions.mainOptions, state, viewModel)
                    }
                }
            }
        }
    }

    @Composable
    private fun Options(
        options: List<SyncTriggerOptions.Entry>,
        state: SyncOptionsViewModel.State,
        viewModel: SyncOptionsViewModel,
    ) {
        options.forEach { option ->
            LabeledCheckbox(
                label = stringResource(option.label),
                checked = option.getter(state.options),
                onCheckedChange = {
                    viewModel.toggle(option.setter, it)
                },
                enabled = option.enabled(state.options),
            )
        }
    }
}

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class SyncOptionsViewModel(
    val syncPreferences: SyncPreferences,
) : ViewModel() {

    val state: StateFlow<SyncOptionsViewModel.State>
        field = MutableStateFlow<SyncOptionsViewModel.State>(State(syncPreferences.getSyncTriggerOptions()))

    fun toggle(setter: (SyncTriggerOptions, Boolean) -> SyncTriggerOptions, enabled: Boolean) {
        state.update {
            val updatedTriggerOptions = setter(it.options, enabled)
            syncPreferences.setSyncTriggerOptions(updatedTriggerOptions)
            it.copy(
                options = updatedTriggerOptions,
            )
        }
    }

    @Immutable
    data class State(
        val options: SyncTriggerOptions = SyncTriggerOptions(),
    )
}
// <-- AM (SYNC)
