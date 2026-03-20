package mihon.core.migration.migrations

import android.app.Application
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.domain.library.service.LibraryPreferences

class RenameEnumMigration : Migration {
    override val version = 81f

    // Handle renamed enum values
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false
        val libraryPreferences = migrationContext.get<LibraryPreferences>() ?: return false
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
