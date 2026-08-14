package eu.kanade.presentation.more.settings.screen.player.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.more.settings.screen.player.editor.codeeditor.CodeEditScreen
import eu.kanade.presentation.more.settings.screen.player.editor.components.EditorScreen
import eu.kanade.presentation.more.settings.screen.player.editor.components.FileCreateDialog
import eu.kanade.presentation.more.settings.screen.player.editor.components.FileDeleteDialog
import eu.kanade.presentation.util.Screen

object PlayerSettingsEditorScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = viewModel<PlayerSettingsEditorViewModel>(
            factory = PlayerSettingsEditorViewModel.Factory,
        )

        val state by viewModel.state.collectAsState()
        val dialog by viewModel.dialogShown.collectAsState()
        val selectedType by viewModel.selectedType.collectAsState()

        when (dialog) {
            null -> {}
            EditorFileDialog.Create -> {
                FileCreateDialog(
                    initialName = null,
                    fileExtension = selectedType.fileExtension,
                    onDismissRequest = viewModel::dismissDialog,
                    isValid = viewModel::isValidName,
                    onConfirm = viewModel::createFile,
                )
            }
            is EditorFileDialog.Edit -> {
                val name = (dialog as EditorFileDialog.Edit).item.name
                FileCreateDialog(
                    initialName = name,
                    fileExtension = selectedType.fileExtension,
                    onDismissRequest = viewModel::dismissDialog,
                    isValid = viewModel::isValidName,
                    onConfirm = { viewModel.editFile(name, it) },
                )
            }
            is EditorFileDialog.Delete -> {
                val name = (dialog as EditorFileDialog.Delete).item.name
                FileDeleteDialog(
                    name = name,
                    onDismissRequest = viewModel::dismissDialog,
                    onDelete = { viewModel.deleteFile(name) },
                )
            }
        }

        EditorScreen(
            state = state,
            selectedType = selectedType,
            onSelectType = viewModel::onSelectType,
            onClickItem = {
                viewModel.getFilePath(it.name).let { filePath ->
                    navigator.push(CodeEditScreen(filePath))
                }
            },
            onRenameItem = { viewModel.showDialog(EditorFileDialog.Edit(it)) },
            onDeleteItem = { viewModel.showDialog(EditorFileDialog.Delete(it)) },
            onClickAdd = { viewModel.showDialog(EditorFileDialog.Create) },
            navigateUp = navigator::pop,
        )
    }
}
