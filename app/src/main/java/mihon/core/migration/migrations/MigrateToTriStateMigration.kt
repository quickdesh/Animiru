package mihon.core.migration.migrations

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.domain.library.service.LibraryPreferences

@Inject
@ContributesIntoSet(AppScope::class)
class MigrateToTriStateMigration(
    private val context: Context,
    private val libraryPreferences: LibraryPreferences,
) : Migration {
    override val version = 52f

    // Migrate library filters to tri-state versions
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        prefs.edit {
            putInt(
                libraryPreferences.filterDownloaded.key(),
                convertBooleanPrefToTriState(prefs, "pref_filter_downloaded_key"),
            )
            remove("pref_filter_downloaded_key")

            putInt(
                libraryPreferences.filterUnseen.key(),
                convertBooleanPrefToTriState(prefs, "pref_filter_unread_key"),
            )
            remove("pref_filter_unread_key")

            putInt(
                libraryPreferences.filterDownloaded.key(),
                convertBooleanPrefToTriState(prefs, "pref_filter_completed_key"),
            )
            remove("pref_filter_completed_key")
        }

        return true
    }

    private fun convertBooleanPrefToTriState(prefs: SharedPreferences, key: String): Int {
        val oldPrefValue = prefs.getBoolean(key, false)
        return if (oldPrefValue) {
            1
        } else {
            0
        }
    }
}
