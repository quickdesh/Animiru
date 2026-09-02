package mihon.core.migration.migrations

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.util.system.workManager
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

@Inject
@ContributesIntoSet(AppScope::class)
class RemoveBackgroundJobsMigration(
    private val context: Context,
) : Migration {
    override val version = 97f

    // Removed background jobs
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        context.workManager.cancelAllWorkByTag("UpdateChecker")
        context.workManager.cancelAllWorkByTag("ExtensionUpdate")
        prefs.edit {
            remove("automatic_ext_updates")
        }

        return true
    }
}
