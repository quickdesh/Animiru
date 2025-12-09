// AM (CONNECTION) -->
package eu.kanade.domain.connection.service

import eu.kanade.tachiyomi.data.connection.Connection
import tachiyomi.core.common.preference.PreferenceStore

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

    companion object {

        fun connectionUsername(connectionId: Long) = "pref_anime_connections_username_$connectionId"

        private fun connectionPassword(connectionId: Long) = "pref_anime_connections_password_$connectionId"

        private fun connectionToken(connectionId: Long) = "connection_token_$connectionId"
    }
}
// <-- AM (CONNECTION)
