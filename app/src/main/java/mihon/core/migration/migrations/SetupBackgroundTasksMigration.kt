package mihon.core.migration.migrations

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

@Inject
@ContributesIntoSet(AppScope::class)
class SetupBackgroundTasksMigration(
    private val context: Context,
) : Migration {
    override val version = 64f

    // Set up background tasks
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        LibraryUpdateJob.setupTask(context)

        return true
    }
}
