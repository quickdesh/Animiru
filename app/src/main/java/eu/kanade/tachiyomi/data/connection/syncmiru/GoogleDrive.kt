// AM (SYNC_DRIVE) -->
package eu.kanade.tachiyomi.data.connection.syncmiru

import android.graphics.Color
import eu.kanade.domain.connection.SyncPreferences
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.connection.BaseConnection
import uy.kohesive.injekt.injectLazy

class GoogleDrive(id: Long) : BaseConnection(id, "Google Drive") {

    override fun getLogo() = R.drawable.ic_google_drive_24dp

    override fun getLogoColor() = Color.TRANSPARENT

    private val syncPreferences: SyncPreferences by injectLazy()

    override fun logout() {
        super.logout()
        syncPreferences.googleDriveRefreshToken().set("")
        syncPreferences.googleDriveAccessToken().set("")
    }

    override val isLoggedIn: Boolean
        get() = syncPreferences.googleDriveRefreshToken().get().isNotBlank()
}
// <-- AM (SYNC_DRIVE)
