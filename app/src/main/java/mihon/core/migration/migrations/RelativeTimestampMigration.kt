package mihon.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.domain.ui.UiPreferences
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.preference.PreferenceStore

@Inject
@ContributesIntoSet(AppScope::class)
class RelativeTimestampMigration(
    private val preferenceStore: PreferenceStore,
    private val uiPreferences: UiPreferences,
) : Migration {
    override val version = 106f

    // Bring back simplified relative timestamp setting
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val pref = preferenceStore.getInt("relative_time", 7)
        if (pref.get() == 0) {
            uiPreferences.relativeTime.set(false)
        }

        return true
    }
}
