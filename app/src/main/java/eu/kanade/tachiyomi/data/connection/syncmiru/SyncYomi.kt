// AM (SYNC_YOMI) -->
package eu.kanade.tachiyomi.data.connection.syncmiru

import android.graphics.Color
import eu.kanade.domain.connection.SyncPreferences
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connection.BaseConnection
import uy.kohesive.injekt.injectLazy

class SyncYomi(id: Long) : BaseConnection(id, "SyncYomi") {

    override fun getLogo() = R.drawable.ic_syncyomi_24dp

    override fun getLogoColor() = Color.TRANSPARENT

    private val syncPreferences: SyncPreferences by injectLazy()

    override fun logout() {
        super.logout()
        syncPreferences.clientHost().set("")
        syncPreferences.clientAPIKey().set("")
    }

    override val isLoggedIn: Boolean
        get() = syncPreferences.clientAPIKey().get().isNotBlank()
}
// <-- AM (SYNC_YOMI)
