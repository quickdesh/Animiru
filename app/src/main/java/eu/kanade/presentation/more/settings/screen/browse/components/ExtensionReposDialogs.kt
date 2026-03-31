package eu.kanade.presentation.more.settings.screen.browse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import eu.kanade.tachiyomi.util.system.pasteFromClipboard
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.delay
import mihon.domain.extensionrepo.model.ExtensionRepo
import tachiyomi.i18n.MR
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.presentation.core.i18n.stringResource
import kotlin.time.Duration.Companion.seconds

@Composable
fun ExtensionRepoCreateDialog(
    onDismissRequest: () -> Unit,
    onCreate: (String) -> Unit,
    repoUrls: ImmutableSet<String>,
) {
    var name by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val nameAlreadyExists = remember(name) { repoUrls.contains(name) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                enabled = name.isNotEmpty() && !nameAlreadyExists,
                onClick = {
                    onCreate(name)
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(MR.strings.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
        title = {
            Text(text = stringResource(MR.strings.action_add_repo))
        },
        text = {
            Column {
                Text(text = stringResource(MR.strings.action_add_repo_message, stringResource(MR.strings.app_name)))

                Row (horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text(text = stringResource(MR.strings.label_add_repo_input))
                        },
                        supportingText = {
                            val msgRes = if (name.isNotEmpty() && nameAlreadyExists) {
                                MR.strings.error_repo_exists
                            } else {
                                MR.strings.information_required_plain
                            }
                            Text(text = stringResource(msgRes))
                        },
                        isError = name.isNotEmpty() && nameAlreadyExists,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        singleLine = true,
                    )
                    IconButton(
                        onClick = {
                            context.pasteFromClipboard()?.let { pastedText -> name = pastedText }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Outlined.ContentPaste,
                            contentDescription = stringResource(AMMR.strings.action_paste_from_clipboard),
                        )
                    }
                }
            }
        },
    )

    LaunchedEffect(focusRequester) {
        // TODO: https://issuetracker.google.com/issues/204502668
        delay(0.1.seconds)
        focusRequester.requestFocus()
    }
}

@Composable
fun ExtensionRepoDeleteDialog(
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    repo: String,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onDelete()
                onDismissRequest()
            }) {
                Text(text = stringResource(MR.strings.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
        title = {
            Text(text = stringResource(MR.strings.action_delete_repo))
        },
        text = {
            Text(text = stringResource(MR.strings.delete_repo_confirmation, repo))
        },
    )
}

@Composable
fun ExtensionRepoConflictDialog(
    oldRepo: ExtensionRepo,
    newRepo: ExtensionRepo,
    onDismissRequest: () -> Unit,
    onMigrate: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onMigrate()
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(MR.strings.action_replace_repo))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
        title = {
            Text(text = stringResource(MR.strings.action_replace_repo_title))
        },
        text = {
            Text(text = stringResource(MR.strings.action_replace_repo_message, newRepo.name, oldRepo.name))
        },
    )
}

@Composable
fun ExtensionRepoConfirmDialog(
    onDismissRequest: () -> Unit,
    onCreate: () -> Unit,
    repo: String,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(MR.strings.action_add_repo))
        },
        text = {
            Text(text = stringResource(MR.strings.add_repo_confirmation, repo))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate()
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(MR.strings.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
    )
}
