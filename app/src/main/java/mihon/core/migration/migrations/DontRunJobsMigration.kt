package mihon.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.preference.getAndSet
import tachiyomi.domain.library.service.LibraryPreferences

@Inject
@ContributesIntoSet(AppScope::class)
class DontRunJobsMigration(
    private val libraryPreferences: LibraryPreferences,
) : Migration {
    override val version = 105f

    // Don't run automatic backup or library update jobs if battery is low
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val pref = libraryPreferences.autoUpdateDeviceRestrictions
        if (pref.isSet() && "battery_not_low" in pref.get()) {
            pref.getAndSet { it - "battery_not_low" }
        }

        return true
    }
}
