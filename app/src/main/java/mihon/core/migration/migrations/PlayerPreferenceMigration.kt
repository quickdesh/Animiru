package mihon.core.migration.migrations

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import animiru.domain.player.service.PlayerPreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

@Inject
@ContributesIntoSet(AppScope::class)
class PlayerPreferenceMigration(
    private val context: Context,
    private val playerPreferences: PlayerPreferences,
) : Migration {
    override val version = 92f

    // add migration for player preference
    @Suppress("SwallowedException")
    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        if (playerPreferences.progressPreference.isSet()) {
            prefs.edit {
                val progressString = try {
                    prefs.getString(playerPreferences.progressPreference.key(), null)
                } catch (e: ClassCastException) {
                    null
                } ?: return@edit
                val newProgress = progressString.toFloatOrNull() ?: return@edit
                putFloat(playerPreferences.progressPreference.key(), newProgress)
            }
        }

        return true
    }
}
