// AM (SYNC) -->
package eu.kanade.tachiyomi.data.connection.syncmiru

import android.graphics.Color
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connection.BaseConnection

class SyncMiru(id: Long) : BaseConnection(id, "Cross Sync") {

    override fun getLogo() = R.drawable.ic_syncmiru_24dp

    override fun getLogoColor() = Color.rgb(24, 0, 34)

    private val syncPreferences by lazy { appGraph.syncPreferences }

    override val isLoggedIn: Boolean
        get() = syncPreferences.isSyncEnabled()
}
// <-- AM (SYNC)
