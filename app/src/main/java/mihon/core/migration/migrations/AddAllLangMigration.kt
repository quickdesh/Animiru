package mihon.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.domain.source.service.SourcePreferences
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.preference.plusAssign

@Inject
@ContributesIntoSet(AppScope::class)
class AddAllLangMigration(
    private val sourcePreferences: SourcePreferences,
) : Migration {
    override val version = 70f

    // Migration to add "all" to enabled langauges
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        if (sourcePreferences.enabledLanguages.isSet()) {
            sourcePreferences.enabledLanguages += "all"
        }

        return true
    }
}
