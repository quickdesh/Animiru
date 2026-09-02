package mihon.core.migration.migrations

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import animiru.domain.player.service.SubtitlePreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

@Inject
@ContributesIntoSet(AppScope::class)
class VideoPlayerPreferenceMigration(
    private val context: Context,
    private val subtitlePreferences: SubtitlePreferences,
    private val json: Json,
) : Migration {
    override val version = 126f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val subtitleConf = prefs.getString("pref_sub_select_conf", "")!!
        val subtitleData = try {
            json.decodeFromString<SubConfig>(subtitleConf)
        } catch (e: SerializationException) {
            return false
        }

        prefs.edit {
            putString(subtitlePreferences.preferredSubLanguages.key(), subtitleData.lang.joinToString(","))
            putString(subtitlePreferences.subtitleWhitelist.key(), subtitleData.whitelist.joinToString(","))
            putString(subtitlePreferences.subtitleBlacklist.key(), subtitleData.blacklist.joinToString(","))
        }

        return true
    }

    @Serializable
    data class SubConfig(
        val lang: List<String> = emptyList(),
        val blacklist: List<String> = emptyList(),
        val whitelist: List<String> = emptyList(),
    )
}
