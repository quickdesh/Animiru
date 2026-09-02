// AM (CONNECTION) -->
package eu.kanade.tachiyomi.data.connection

import android.content.Context
import androidx.annotation.CallSuper
import eu.kanade.domain.connection.service.ConnectionPreferences
import eu.kanade.tachiyomi.network.NetworkHelper
import mihon.app.di.appGraph
import okhttp3.OkHttpClient
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

abstract class BaseConnection(
    override val id: Long,
    override val name: String,
) : Connection {

    // TODO: remove injekt usage
    protected val appGraph get() = Injekt.get<Context>().appGraph

    private val connectionPreferences: ConnectionPreferences by lazy { appGraph.connectionPreferences }
    private val networkService: NetworkHelper by lazy { appGraph.networkHelper }

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
