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
class MergeSortTypeDirectionMigration(
    private val context: Context,
    private val libraryPreferences: LibraryPreferences,
) : Migration {
    override val version = 82f

    // Merge Sort Type and Direction into one class
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        prefs.edit {
            val animesort = prefs.getString(
                libraryPreferences.sortingMode.key(),
                null,
            ) ?: return@edit
            val direction = prefs.getString("library_sorting_ascending", "ASCENDING")!!
            putString(libraryPreferences.sortingMode.key(), "$animesort,$direction")
            remove("library_sorting_ascending")
        }

        return true
    }
}
