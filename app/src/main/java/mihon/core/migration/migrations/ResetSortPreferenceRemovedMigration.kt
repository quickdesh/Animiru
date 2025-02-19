package mihon.core.migration.migrations

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.App
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.domain.library.service.LibraryPreferences

class ResetSortPreferenceRemovedMigration : Migration {
    override val version = 44f

    // Reset sorting preference if using removed sort by source
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<App>() ?: return false
        val libraryPreferences = migrationContext.get<LibraryPreferences>() ?: return false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val oldAnimeSortingMode = prefs.getInt(
            libraryPreferences.animeSortingMode().key(),
            0,
        )

        if (oldAnimeSortingMode == 5) { // SOURCE = 5
            prefs.edit {
                putInt(libraryPreferences.animeSortingMode().key(), 0) // ALPHABETICAL = 0
            }
        }

        return true
    }
}
