// AM (SYNC) -->
package eu.kanade.tachiyomi.data.connection.syncmiru

import android.graphics.Color
import eu.kanade.domain.connection.SyncPreferences
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connection.BaseConnection
import uy.kohesive.injekt.injectLazy

class SyncMiru(id: Long) : BaseConnection(id, "Cross Sync") {

    override fun getLogo() = R.drawable.ic_syncmiru_24dp

    override fun getLogoColor() = Color.rgb(24, 0, 34)

    private val syncPreferences: SyncPreferences by injectLazy()

    override val isLoggedIn: Boolean
        get() = syncPreferences.isSyncEnabled()
}
// <-- AM (SYNC)
