package mihon.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

@Inject
@ContributesIntoSet(AppScope::class)
class PrivatePreferenceMigration(
    private val preferenceStore: PreferenceStore,
) : Migration {
    override val version = 116f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        replacePreferences(
            preferenceStore = preferenceStore,
            filterPredicate = { it.key.startsWith("pref_mangasync_") || it.key.startsWith("track_token_") },
            newKey = { Preference.privateKey(it) },
        )

        return true
    }
}
