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
class DeleteExternalChapterCacheDirMigration(
    private val context: Context,
) : Migration {
    override val version = 26f

    // Delete external chapter cache dir.
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val extCache = context.externalCacheDir
        if (extCache != null) {
            val chapterCache = File(extCache, "chapter_disk_cache")
            if (chapterCache.exists()) {
                chapterCache.deleteRecursively()
            }
        }

        return true
    }
}
