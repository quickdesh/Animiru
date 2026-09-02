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
class MoveEpisodePreferencesMigration(
    private val context: Context,
    private val libraryPreferences: LibraryPreferences,
) : Migration {
    override val version = 85f

    // Move chapter preferences from PreferencesHelper to LibraryPrefrences
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val preferences = listOf(
            libraryPreferences.filterEpisodeBySeen,
            libraryPreferences.filterEpisodeByDownloaded,
            libraryPreferences.filterEpisodeByBookmarked,
            libraryPreferences.sortEpisodeBySourceOrNumber,
            libraryPreferences.displayEpisodeByNameOrNumber,
            libraryPreferences.sortEpisodeByAscendingOrDescending,
        )

        prefs.edit {
            preferences.forEach { preference ->
                val key = preference.key()
                val value = prefs.getInt(key, Int.MIN_VALUE)
                if (value == Int.MIN_VALUE) return@forEach
                remove(key)
                putLong(key, value.toLong())
            }
        }

        return true
    }
}
