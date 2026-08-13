// AM (CONNECTION) -->
package eu.kanade.presentation.more.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.tachiyomi.data.connection.Connection
import eu.kanade.tachiyomi.data.connection.ConnectionManager
import eu.kanade.tachiyomi.util.system.openDiscordLoginActivity
import eu.kanade.tachiyomi.util.system.toast
import tachiyomi.i18n.MR
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object SettingsConnectionScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = AMMR.strings.pref_category_connection

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val connectionManager = remember { Injekt.get<ConnectionManager>() }

        return listOf(
            Preference.PreferenceGroup(
                title = stringResource(AMMR.strings.special_services),
                preferenceItems = listOf(
                    // AM (DISCORD_RPC) -->
                    Preference.PreferenceItem.ConnectionPreference(
                        title = connectionManager.discord.name,
                        connection = connectionManager.discord,
                        login = { context.openDiscordLoginActivity() },
                        openSettings = { navigator.push(SettingsDiscordScreen) },
                    ),
                    // <-- AM (DISCORD_RPC)
                    // AM (SYNC) -->
                    Preference.PreferenceItem.ConnectionPreference(
                        title = connectionManager.syncmiru.name,
                        connection = connectionManager.syncmiru,
                        login = { navigator.push(SettingsSyncmiruScreen) },
                        openSettings = { navigator.push(SettingsSyncmiruScreen) },
                    ),
                    // <-- AM (SYNC)
                    // AM (DISCORD_RPC) -->
                    Preference.PreferenceItem.InfoPreference(stringResource(AMMR.strings.connection_discord_info)),
                    // <-- AM (DISCORD_RPC)
                ),
            ),
        )
    }
}

@Composable
internal fun ConnectionLogoutDialog(
    connection: Connection,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(MR.strings.logout_title, connection.name),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDismissRequest,
                ) {
                    Text(text = stringResource(MR.strings.action_cancel))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        connection.logout()
                        onDismissRequest()
                        context.toast(MR.strings.logout_success)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(text = stringResource(MR.strings.logout))
                }
            }
        },
    )
}

internal data class LoginConnectionDialog(
    val connection: Connection,
)

internal data class LogoutConnectionDialog(
    val connection: Connection,
)
// <-- AM (CONNECTION)
