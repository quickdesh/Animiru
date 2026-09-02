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
class InternalChapterCacheDirMigration(
    private val context: Context,
) : Migration {
    override val version = 15f

    // Delete internal chapter cache dir.
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        File(context.cacheDir, "chapter_disk_cache").deleteRecursively()

        return true
    }
}
