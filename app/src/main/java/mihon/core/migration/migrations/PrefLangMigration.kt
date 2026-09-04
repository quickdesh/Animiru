package mihon.core.migration.migrations

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import animiru.domain.player.service.AudioPreferences
import animiru.domain.player.service.SubtitlePreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import java.util.Locale
import java.util.MissingResourceException

@Inject
@ContributesIntoSet(AppScope::class)
class PrefLangMigration(
    private val context: Context,
    private val audioPreferences: AudioPreferences,
    private val subtitlePreferences: SubtitlePreferences,
) : Migration {
    override val version = 130f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        listOf(
            audioPreferences.preferredAudioLanguages,
            subtitlePreferences.preferredSubLanguages,
        ).forEach { pref ->
            if (pref.isSet()) {
                prefs.edit {
                    val langs = prefs.getString(
                        pref.key(),
                        "",
                    )!!.split(",").filter(String::isNotEmpty).map(String::trim)
                    val newLangs = langs.filter { it.isValidCode() }.joinToString(",")
                    putString(pref.key(), newLangs)
                }
            }
        }

        return true
    }

    private fun String.isValidCode(): Boolean {
        try {
            val locale = Locale(this)
            if (locale.isO3Language == locale.language && locale.language == locale.getDisplayName(Locale.ENGLISH)) {
                return false
            }
        } catch (_: MissingResourceException) {
            return false
        }

        return true
    }
}
