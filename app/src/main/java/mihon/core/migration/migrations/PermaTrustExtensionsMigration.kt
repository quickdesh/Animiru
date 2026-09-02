package mihon.core.migration.migrations

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.preference.Preference

@Inject
@ContributesIntoSet(AppScope::class)
class PermaTrustExtensionsMigration(
    private val context: Context,
) : Migration {
    override val version = 117f

    // Allow permanently trusting unofficial extensions by version code + signature
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        prefs.edit {
            remove(Preference.appStateKey("trusted_signatures"))
        }

        return true
    }
}
