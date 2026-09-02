package mihon.core.migration.migrations

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import java.io.File

@Inject
@ContributesIntoSet(AppScope::class)
class CoverToExternalFileMigration(
    private val context: Context,
) : Migration {
    override val version = 19f

    // Move covers to external files dir.
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val oldDir = File(context.externalCacheDir, "cover_disk_cache")
        if (oldDir.exists()) {
            val destDir = context.getExternalFilesDir("covers")
            if (destDir != null) {
                oldDir.listFiles()?.forEach {
                    it.renameTo(File(destDir, it.name))
                }
            }
        }

        return true
    }
}
