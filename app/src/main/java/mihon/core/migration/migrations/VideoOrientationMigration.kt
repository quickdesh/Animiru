package mihon.core.migration.migrations

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import animiru.domain.player.model.PlayerOrientation
import animiru.domain.player.service.PlayerPreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.preference.getEnum

@Inject
@ContributesIntoSet(AppScope::class)
class VideoOrientationMigration(
    private val context: Context,
    private val playerPreferences: PlayerPreferences,
    private val preferenceStore: PreferenceStore,
) : Migration {
    override val version = 127f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val oldPref = try {
            prefs.getInt(
                playerPreferences.defaultPlayerOrientationType.key(),
                10,
            )
        } catch (_: ClassCastException) {
            prefs.edit(commit = true) {
                remove(playerPreferences.defaultPlayerOrientationType.key())
            }
            return true
        }

        val newPref = when (oldPref) {
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR -> PlayerOrientation.Free
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> PlayerOrientation.Portrait
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> PlayerOrientation.ReversePortrait
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> PlayerOrientation.Landscape
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> PlayerOrientation.ReverseLandscape
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT -> PlayerOrientation.SensorPortrait
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> PlayerOrientation.SensorLandscape
            else -> PlayerOrientation.Free
        }

        prefs.edit(commit = true) {
            remove(playerPreferences.defaultPlayerOrientationType.key())
        }

        prefs.edit {
            preferenceStore.getEnum(
                playerPreferences.defaultPlayerOrientationType.key(),
                PlayerOrientation.SensorLandscape,
            ).set(newPref)
        }

        return true
    }
}
