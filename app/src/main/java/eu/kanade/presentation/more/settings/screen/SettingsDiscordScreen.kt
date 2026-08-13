// AM (DISCORD_RPC) -->
package eu.kanade.presentation.more.settings.screen

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.util.fastMap
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.connection.service.ConnectionPreferences
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.widget.TriStateListDialog
import eu.kanade.tachiyomi.data.connection.ConnectionManager
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.runBlocking
import tachiyomi.domain.category.interactor.GetCategories
import tachiyomi.i18n.MR
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object SettingsDiscordScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = AMMR.strings.pref_category_connection

    @Composable
    override fun RowScope.AppBarAction() {
        val uriHandler = LocalUriHandler.current
        IconButton(onClick = { uriHandler.openUri("https://aniyomi.org/docs/guides/tracking") }) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = stringResource(MR.strings.tracking_guide),
            )
        }
    }

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val connectionPreferences = remember { Injekt.get<ConnectionPreferences>() }
        val connectionManager = remember { Injekt.get<ConnectionManager>() }
        val enableDRPCPref = connectionPreferences.enableDiscordRPC
        val discordRPCStatus = connectionPreferences.discordRPCStatus

        val enableDRPC by enableDRPCPref.collectAsState()

        var dialog by remember { mutableStateOf<Any?>(null) }
        dialog?.run {
            when (this) {
                is LogoutConnectionDialog -> {
                    ConnectionLogoutDialog(
                        connection = connection,
                        onDismissRequest = {
                            dialog = null
                            enableDRPCPref.set(false)
                            navigator.pop()
                        },
                    )
                }
            }
        }

        return listOf(
            Preference.PreferenceGroup(
                title = stringResource(AMMR.strings.connection_discord),
                preferenceItems = listOf(
                    Preference.PreferenceItem.SwitchPreference(
                        preference = enableDRPCPref,
                        title = stringResource(AMMR.strings.pref_enable_discord_rpc),
                    ),
                    Preference.PreferenceItem.ListPreference(
                        preference = discordRPCStatus,
                        title = stringResource(AMMR.strings.pref_discord_status),
                        entries = mapOf(
                            -1 to stringResource(AMMR.strings.pref_discord_dnd),
                            0 to stringResource(AMMR.strings.pref_discord_idle),
                            1 to stringResource(AMMR.strings.pref_discord_online),
                        ),
                        enabled = enableDRPC,
                        onValueChanged = {
                            context.toast(MR.strings.requires_app_restart)
                            true
                        },
                    ),
                ),
            ),
            getRPCIncognitoGroup(
                connectionPreferences = connectionPreferences,
                enabled = enableDRPC,
            ),
            Preference.PreferenceItem.TextPreference(
                title = stringResource(MR.strings.logout),
                onClick = { dialog = LogoutConnectionDialog(connectionManager.discord) },
            ),
        )
    }

    @Composable
    private fun getRPCIncognitoGroup(
        connectionPreferences: ConnectionPreferences,
        enabled: Boolean,
    ): Preference.PreferenceGroup {
        val getAnimeCategories = remember { Injekt.get<GetCategories>() }
        val allAnimeCategories by getAnimeCategories.subscribe().collectAsState(
            initial = runBlocking {
                getAnimeCategories.await()
            },
        )

        val discordRPCIncognitoPref = connectionPreferences.discordRPCIncognito
        val discordRPCIncognitoCategoriesPref = connectionPreferences.discordRPCIncognitoCategories

        val includedAnime by discordRPCIncognitoCategoriesPref.collectAsState()
        var showAnimeDialog by rememberSaveable { mutableStateOf(false) }
        if (showAnimeDialog) {
            TriStateListDialog(
                title = stringResource(MR.strings.categories),
                message = stringResource(AMMR.strings.pref_discord_incognito_categories_details),
                items = allAnimeCategories,
                initialChecked = includedAnime.mapNotNull { id -> allAnimeCategories.find { it.id.toString() == id } },
                initialInversed = includedAnime.mapNotNull { allAnimeCategories.find { false } },
                itemLabel = { it.visualName },
                onDismissRequest = { showAnimeDialog = false },
                onValueChanged = { newIncluded, _ ->
                    discordRPCIncognitoCategoriesPref.set(
                        newIncluded.fastMap { it.id.toString() }
                            .toSet(),
                    )
                    showAnimeDialog = false
                },
                onlyChecked = true,
            )
        }

        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.categories),
            preferenceItems = listOf(
                Preference.PreferenceItem.SwitchPreference(
                    preference = discordRPCIncognitoPref,
                    title = stringResource(AMMR.strings.pref_discord_incognito),
                    subtitle = stringResource(AMMR.strings.pref_discord_incognito_summary),
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(MR.strings.categories),
                    subtitle = getCategoriesLabel(
                        allCategories = allAnimeCategories,
                        included = includedAnime,
                    ),
                    onClick = { showAnimeDialog = true },
                ),
                Preference.PreferenceItem.InfoPreference(
                    stringResource(AMMR.strings.pref_discord_incognito_categories_details),
                ),
            ),
            enabled = enabled,
        )
    }
}
// <-- AM (DISCORD_RPC)
