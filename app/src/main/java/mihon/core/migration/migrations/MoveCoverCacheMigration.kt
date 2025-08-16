package mihon.core.migration.migrations

import android.app.Application
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import java.io.File

class MoveCoverCacheMigration : Migration {
    override val version = 131f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val context = migrationContext.get<Application>() ?: return false

        val oldCacheDir = context.getExternalFilesDir("animecovers")
            ?: File(context.filesDir, "animecovers")
        val newCacheDir = File(oldCacheDir.parentFile, "covers")

        return oldCacheDir.renameTo(newCacheDir)
    }
}
