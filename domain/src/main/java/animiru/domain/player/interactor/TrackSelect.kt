package animiru.domain.player.interactor

import androidx.core.os.LocaleListCompat
import animiru.domain.player.model.VideoTrack
import animiru.domain.player.service.AudioPreferences
import animiru.domain.player.service.SubtitlePreferences
import dev.zacsweers.metro.Inject
import java.util.Locale

@Inject
class TrackSelect(
    private val subtitlePreferences: SubtitlePreferences,
    private val audioPreferences: AudioPreferences,
) {
    fun <T : VideoTrack> getPreferredTrackIndex(tracks: List<T>, subtitle: Boolean = true): T? {
        val prefLangs = if (subtitle) {
            subtitlePreferences.preferredSubLanguages.get()
        } else {
            audioPreferences.preferredAudioLanguages.get()
        }.split(",").filter(String::isNotEmpty).map(String::trim)

        val whitelist = if (subtitle) {
            subtitlePreferences.subtitleWhitelist.get()
        } else {
            ""
        }.split(",").filter(String::isNotEmpty).map(String::trim)

        val blacklist = if (subtitle) {
            subtitlePreferences.subtitleBlacklist.get()
        } else {
            ""
        }.split(",").filter(String::isNotEmpty).map(String::trim)

        val locales = prefLangs.map(::Locale).ifEmpty {
            listOf(LocaleListCompat.getDefault()[0]!!)
        }

        val chosenLocale = locales.firstOrNull { locale ->
            tracks.any { t -> containsLang(t, locale) }
        }

        val filtered = tracks.withIndex()
            .filterNot { (_, track) ->
                blacklist.any { track.title.contains(it, true) }
            }
            .filter { (_, track) ->
                chosenLocale?.let { containsLang(track, it) } ?: true
            }

        whitelist.forEach { w ->
            filtered.firstOrNull { (_, track) ->
                track.title.contains(w, true)
            }?.let { return it.value }
        }

        return filtered.getOrNull(0)?.value
    }

    private fun containsLang(track: VideoTrack, locale: Locale): Boolean {
        val localName = locale.getDisplayName(locale)
        val englishName = locale.getDisplayName(Locale.ENGLISH).substringBefore(" (")
        val langRegex = Regex("""\b${locale.isO3Language}|${locale.language}\b""", RegexOption.IGNORE_CASE)
        val trackTitle = track.title

        return trackTitle.contains(localName, true) ||
            trackTitle.contains(englishName, true) ||
            track.language.let { langRegex.find(it) != null }
    }
}
