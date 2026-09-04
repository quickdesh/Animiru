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
class RenameEnumMigration(
    private val context: Context,
    private val libraryPreferences: LibraryPreferences,
) : Migration {
    override val version = 81f

    // Handle renamed enum values
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        prefs.edit {
            val newAnimeSortingMode = when (
                val oldSortingMode = prefs.getString(
                    libraryPreferences.sortingMode.key(),
                    "ALPHABETICAL",
                )
            ) {
                "LAST_CHECKED" -> "LAST_MANGA_UPDATE"
                "UNREAD" -> "UNREAD_COUNT"
                "DATE_FETCHED" -> "CHAPTER_FETCH_DATE"
                else -> oldSortingMode
            }
            putString(libraryPreferences.sortingMode.key(), newAnimeSortingMode)
        }

        return true
    }
}
