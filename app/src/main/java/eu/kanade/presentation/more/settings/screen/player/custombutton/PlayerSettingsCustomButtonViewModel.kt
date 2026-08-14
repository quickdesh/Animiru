package eu.kanade.presentation.more.settings.screen.player.custombutton

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import animiru.feature.mpvfiles.MpvConfig
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mihon.core.viewmodel.StateViewModel
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.domain.custombutton.interactor.CreateCustomButton
import tachiyomi.domain.custombutton.interactor.DeleteCustomButton
import tachiyomi.domain.custombutton.interactor.GetCustomButtons
import tachiyomi.domain.custombutton.interactor.ReorderCustomButton
import tachiyomi.domain.custombutton.interactor.ToggleFavoriteCustomButton
import tachiyomi.domain.custombutton.interactor.UpdateCustomButton
import tachiyomi.domain.custombutton.model.CustomButton
import tachiyomi.domain.custombutton.model.CustomButtonUpdate
import tachiyomi.i18n.MR
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PlayerSettingsCustomButtonViewModel(
    private val getCustomButtons: GetCustomButtons = Injekt.get(),
    private val createCustomButton: CreateCustomButton = Injekt.get(),
    private val deleteCustomButton: DeleteCustomButton = Injekt.get(),
    private val updateCustomButton: UpdateCustomButton = Injekt.get(),
    private val reorderCustomButton: ReorderCustomButton = Injekt.get(),
    private val toggleFavoriteCustomButton: ToggleFavoriteCustomButton = Injekt.get(),
    // AM -->
    private val mpvConfig: MpvConfig = Injekt.get(),
    // <-- AM
) : StateViewModel<CustomButtonScreenState>(CustomButtonScreenState.Loading) {

    private val _events: Channel<CustomButtonEvent> = Channel()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            getCustomButtons.subscribeAll()
                .collectLatest { customButtons ->
                    mutableState.update {
                        CustomButtonScreenState.Success(
                            customButtons = customButtons,
                        )
                    }
                }
        }

        // AM -->
        viewModelScope.launchIO {
            getCustomButtons.subscribeAll()
                .distinctUntilChanged()
                .collectLatest { customButtons ->
                    mpvConfig.setupCustomButtons(customButtons)
                }
        }
        // <-- AM
    }

    fun createCustomButton(name: String, content: String, longPressContent: String, onStartup: String) {
        viewModelScope.launch {
            when (createCustomButton.await(name, content, longPressContent, onStartup)) {
                is CreateCustomButton.Result.InternalError -> _events.send(
                    CustomButtonEvent.InternalError,
                )
                else -> {}
            }
        }
    }

    fun togglePrimaryButton(customButton: CustomButton) {
        viewModelScope.launch {
            when (toggleFavoriteCustomButton.await(customButton)) {
                is ToggleFavoriteCustomButton.Result.InternalError -> _events.send(
                    CustomButtonEvent.InternalError,
                )
                else -> {}
            }
        }
    }

    fun editCustomButton(customButtonUpdate: CustomButtonUpdate) {
        viewModelScope.launch {
            when (updateCustomButton.await(customButtonUpdate)) {
                is UpdateCustomButton.Result.InternalError -> _events.send(
                    CustomButtonEvent.InternalError,
                )
                else -> {}
            }
        }
    }

    fun deleteCustomButton(customButton: CustomButton) {
        viewModelScope.launch {
            when (deleteCustomButton.await(customButton.id)) {
                is DeleteCustomButton.Result.InternalError -> _events.send(
                    CustomButtonEvent.InternalError,
                )
                else -> {}
            }
        }
    }

    fun changeOrder(customButton: CustomButton, newIndex: Int) {
        viewModelScope.launch {
            when (reorderCustomButton.changeOrder(customButton, newIndex)) {
                is ReorderCustomButton.Result.InternalError -> _events.send(
                    CustomButtonEvent.InternalError,
                )
                else -> {}
            }
        }
    }

    fun showDialog(dialog: CustomButtonDialog) {
        mutableState.update {
            when (it) {
                CustomButtonScreenState.Loading -> it
                is CustomButtonScreenState.Success -> it.copy(dialog = dialog)
            }
        }
    }

    fun dismissDialog() {
        mutableState.update {
            when (it) {
                CustomButtonScreenState.Loading -> it
                is CustomButtonScreenState.Success -> it.copy(dialog = null)
            }
        }
    }
}

sealed interface CustomButtonDialog {
    data object Create : CustomButtonDialog
    data class Edit(val customButton: CustomButton) : CustomButtonDialog
    data class Delete(val customButton: CustomButton) : CustomButtonDialog
}

sealed interface CustomButtonEvent {
    sealed class LocalizedMessage(val stringRes: StringResource) : CustomButtonEvent
    data object InternalError : LocalizedMessage(MR.strings.internal_error)
}

sealed interface CustomButtonScreenState {
    @Immutable
    data object Loading : CustomButtonScreenState

    @Immutable
    data class Success(
        val customButtons: List<CustomButton>,
        val dialog: CustomButtonDialog? = null,
    ) : CustomButtonScreenState {
        val isEmpty: Boolean
            get() = customButtons.isEmpty()
    }
}
