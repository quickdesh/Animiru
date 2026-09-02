// AM (SYNC_DRIVE) -->
package eu.kanade.tachiyomi.ui.setting.connection

import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import eu.kanade.tachiyomi.ui.setting.track.BaseOAuthLoginActivity
import mihon.app.di.appGraph
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.i18n.animiru.AMMR

class GoogleDriveLoginActivity : BaseOAuthLoginActivity() {

    override fun handleResult(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val error = uri.getQueryParameter("error")
        if (code != null) {
            lifecycleScope.launchIO {
                appGraph.googleDriveService.handleAuthorizationCode(
                    code,
                    this@GoogleDriveLoginActivity,
                    onSuccess = {
                        Toast.makeText(
                            this@GoogleDriveLoginActivity,
                            stringResource(AMMR.strings.google_drive_login_success),
                            Toast.LENGTH_LONG,
                        ).show()

                        returnToSettings()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@GoogleDriveLoginActivity,
                            stringResource(AMMR.strings.google_drive_login_failed, error),
                            Toast.LENGTH_LONG,
                        ).show()
                        returnToSettings()
                    },
                )
            }
        } else if (error != null) {
            Toast.makeText(
                this@GoogleDriveLoginActivity,
                stringResource(AMMR.strings.google_drive_login_failed, error),
                Toast.LENGTH_LONG,
            ).show()

            returnToSettings()
        } else {
            returnToSettings()
        }
    }
}
// <-- AM (SYNC_DRIVE)
