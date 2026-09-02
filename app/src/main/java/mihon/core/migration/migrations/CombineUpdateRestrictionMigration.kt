package mihon.core.migration.migrations

import android.content.Context
import androidx.preference.PreferenceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.preference.minusAssign
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.library.service.LibraryPreferences.Companion.ANIME_NON_COMPLETED

@Inject
@ContributesIntoSet(AppScope::class)
class CombineUpdateRestrictionMigration(
    private val context: Context,
    private val libraryPreferences: LibraryPreferences,
) : Migration {
    override val version = 72f

    // Combine global update item restrictions
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val oldUpdateOngoingOnly = prefs.getBoolean(
            "pref_update_only_non_completed_key",
            true,
        )
        if (!oldUpdateOngoingOnly) {
            libraryPreferences.autoUpdateAnimeRestrictions -= ANIME_NON_COMPLETED
        }

        return true
    }
}
