package mihon.core.migration.migrations

import android.content.Context
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
class ResetSortPreferenceRemovedMigration(
    private val context: Context,
    private val libraryPreferences: LibraryPreferences,
) : Migration {
    override val version = 44f

    // Reset sorting preference if using removed sort by source
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val oldAnimeSortingMode = prefs.getInt(
            libraryPreferences.sortingMode.key(),
            0,
        )

        if (oldAnimeSortingMode == 5) { // SOURCE = 5
            prefs.edit {
                putInt(libraryPreferences.sortingMode.key(), 0) // ALPHABETICAL = 0
            }
        }

        return true
    }
}
