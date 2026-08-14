package eu.kanade.presentation.more.settings.screen.player.custombutton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.more.settings.screen.player.custombutton.components.CustomButtonCreateDialog
import eu.kanade.presentation.more.settings.screen.player.custombutton.components.CustomButtonDeleteDialog
import eu.kanade.presentation.more.settings.screen.player.custombutton.components.CustomButtonEditDialog
import eu.kanade.presentation.more.settings.screen.player.custombutton.components.CustomButtonScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.domain.custombutton.model.CustomButtonUpdate
import tachiyomi.presentation.core.screens.LoadingScreen

object PlayerSettingsCustomButtonScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current
        val viewModel = viewModel<PlayerSettingsCustomButtonViewModel>()

        val state by viewModel.state.collectAsState()

        if (state is CustomButtonScreenState.Loading) {
            LoadingScreen()
            return
        }

        val successState = state as CustomButtonScreenState.Success

        CustomButtonScreen(
            state = successState,
            onClickFAQ = { uriHandler.openUri("https://aniyomi.org/docs/guides/player-settings/custom-buttons") },
            onClickCreate = { viewModel.showDialog(CustomButtonDialog.Create) },
            onClickPrimary = { viewModel.togglePrimaryButton(it) },
            onClickEdit = { viewModel.showDialog(CustomButtonDialog.Edit(it)) },
            onClickDelete = { viewModel.showDialog(CustomButtonDialog.Delete(it)) },
            onChangeOrder = viewModel::changeOrder,
            navigateUp = navigator::pop,
        )

        when (val dialog = successState.dialog) {
            null -> {}
            CustomButtonDialog.Create -> {
                CustomButtonCreateDialog(
                    onDismissRequest = viewModel::dismissDialog,
                    onCreate = viewModel::createCustomButton,
                    buttonNames = successState.customButtons.fastMap { it.name },
                )
            }
            is CustomButtonDialog.Delete -> {
                CustomButtonDeleteDialog(
                    onDismissRequest = viewModel::dismissDialog,
                    onDelete = { viewModel.deleteCustomButton(dialog.customButton) },
                    buttonTitle = dialog.customButton.name,
                )
            }
            is CustomButtonDialog.Edit -> {
                CustomButtonEditDialog(
                    onDismissRequest = viewModel::dismissDialog,
                    onEdit = { title, content, longPressContent, onStartup ->
                        viewModel.editCustomButton(
                            CustomButtonUpdate(
                                id = dialog.customButton.id,
                                name = title,
                                sortIndex = dialog.customButton.sortIndex,
                                content = content,
                                longPressContent = longPressContent,
                                onStartup = onStartup,
                            ),
                        )
                    },
                    buttonNames = (successState.customButtons - dialog.customButton).fastMap {
                        it.name
                    },
                    initialState = dialog.customButton,
                )
            }
        }

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                if (event is CustomButtonEvent.LocalizedMessage) {
                    context.toast(event.stringRes)
                }
            }
        }
    }
}
