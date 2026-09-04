package mihon.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.track.TrackerManager
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

@Inject
@ContributesIntoSet(AppScope::class)
class ForceMALLogOutMigration(
    private val trackerManager: Lazy<TrackerManager>,
) : Migration {
    override val version = 54f

    // Force MAL log out due to login flow change
    // v52: switched from scraping to WebView
    // v53: switched from WebView to OAuth
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        if (trackerManager.value.myAnimeList.isLoggedIn) {
            trackerManager.value.myAnimeList.logout()
        }

        return true
    }
}
