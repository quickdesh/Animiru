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
class MoveCoverCacheMigration(
    private val context: Context,
) : Migration {
    override val version = 131f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val oldCacheDir = context.getExternalFilesDir("animecovers")
            ?: File(context.filesDir, "animecovers")
        val newCacheDir = File(oldCacheDir.parentFile, "covers")

        return oldCacheDir.renameTo(newCacheDir)
    }
}
