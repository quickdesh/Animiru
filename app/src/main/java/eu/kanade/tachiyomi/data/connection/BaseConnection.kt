// AM (CONNECTION) -->
package eu.kanade.tachiyomi.data.connection

import androidx.annotation.CallSuper
import eu.kanade.domain.connection.service.ConnectionPreferences
import eu.kanade.tachiyomi.network.NetworkHelper
import okhttp3.OkHttpClient
import uy.kohesive.injekt.injectLazy

abstract class BaseConnection(
    override val id: Long,
    override val name: String,
) : Connection {

    private val connectionPreferences: ConnectionPreferences by injectLazy()
    private val networkService: NetworkHelper by injectLazy()

    override val client: OkHttpClient
        get() = networkService.client

    // Name of the connection service to display

    override suspend fun login(username: String, password: String) {
        // Not Needed
    }

    @CallSuper
    override fun logout() {
        connectionPreferences.setConnectionCredentials(this, "", "")
        connectionPreferences.connectionToken(this).set("")
    }

    override fun getUsername() = connectionPreferences.connectionUsername(this).get()

    override fun getPassword() = connectionPreferences.connectionPassword(this).get()

    override fun saveCredentials(username: String, password: String) {
        connectionPreferences.setConnectionCredentials(this, username, password)
    }
}
// <-- AM (CONNECTION)
