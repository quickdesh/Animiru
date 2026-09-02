package mihon.core.migration.migrations

import android.content.Context
import androidx.preference.PreferenceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.domain.base.BasePreferences
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import eu.kanade.tachiyomi.util.system.DeviceUtil
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

@Inject
@ContributesIntoSet(AppScope::class)
class MigrateSecureScreenMigration(
    private val context: Context,
    private val securityPreferences: SecurityPreferences,
    private val basePreferences: BasePreferences,
) : Migration {
    override val version = 75f

    // Allow disabling secure screen when incognito mode is on
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val oldSecureScreen = prefs.getBoolean("secure_screen", false)
        if (oldSecureScreen) {
            securityPreferences.secureScreen.set(
                SecurityPreferences.SecureScreenMode.ALWAYS,
            )
        }
        if (DeviceUtil.isMiui &&
            basePreferences.extensionInstaller.get() == BasePreferences.ExtensionInstaller.PACKAGEINSTALLER
        ) {
            basePreferences.extensionInstaller.set(
                BasePreferences.ExtensionInstaller.LEGACY,
            )
        }

        return true
    }
}
