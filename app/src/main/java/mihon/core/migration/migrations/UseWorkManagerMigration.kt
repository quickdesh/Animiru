package mihon.core.migration.migrations

import android.app.Application
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

class UseWorkManagerMigration : Migration {
    override val version = 96f

    // Fully utilize WorkManager for library updates
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false

        LibraryUpdateJob.cancelAllWorks(context)
        LibraryUpdateJob.setupTask(context)

        return true
    }
}
