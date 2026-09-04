// AM (CONNECTION) -->
package eu.kanade.domain.connection.service

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.data.connection.Connection
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

@Inject
@SingleIn(AppScope::class)
class ConnectionPreferences(
    private val preferenceStore: PreferenceStore,
) {
    fun connectionUsername(connection: Connection) = preferenceStore.getString(
        connectionUsername(connection.id),
        "",
    )

    fun connectionPassword(connection: Connection) = preferenceStore.getString(
        connectionPassword(connection.id),
        "",
    )

    fun setConnectionCredentials(connection: Connection, username: String, password: String) {
        connectionUsername(connection).set(username)
        connectionPassword(connection).set(password)
    }

    fun connectionToken(connection: Connection) = preferenceStore.getString(connectionToken(connection.id), "")

    // AM (DISCORD_RPC) -->
    val enableDiscordRPC: Preference<Boolean> = preferenceStore.getBoolean("pref_enable_discord_rpc", false)

    val discordRPCStatus: Preference<Int> = preferenceStore.getInt("pref_discord_rpc_status", 1)

    val discordRPCIncognito: Preference<Boolean> = preferenceStore.getBoolean("pref_discord_rpc_incognito", false)

    val discordRPCIncognitoCategories: Preference<Set<String>> = preferenceStore.getStringSet(
        "discord_rpc_incognito_categories",
        emptySet(),
    )

    val discordCustomMessage = preferenceStore.getString("pref_discord_custom_message", "")

    val discordShowProgress = preferenceStore.getBoolean("pref_discord_show_progress", true)

    val discordShowEpisodeTitle = preferenceStore.getBoolean("pref_discord_show_episode_title", true)

    val discordShowTimestamp = preferenceStore.getBoolean("pref_discord_show_timestamp", true)

    val discordShowButtons = preferenceStore.getBoolean("pref_discord_show_buttons", true)

    val discordShowDownloadButton = preferenceStore.getBoolean("pref_discord_show_download_button", true)

    val discordShowDiscordButton = preferenceStore.getBoolean("pref_discord_show_discord_button", true)
    // <-- AM (DISCORD_RPC)

    companion object {

        fun connectionUsername(connectionId: Long) = "pref_anime_connections_username_$connectionId"

        private fun connectionPassword(connectionId: Long) = "pref_anime_connections_password_$connectionId"

        private fun connectionToken(connectionId: Long) = "connection_token_$connectionId"
    }
}
// <-- AM (CONNECTION)
