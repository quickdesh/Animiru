package eu.kanade.tachiyomi.util.episode

import eu.kanade.tachiyomi.data.download.DownloadCache
import tachiyomi.domain.episode.model.Episode
import tachiyomi.domain.anime.model.Anime
import tachiyomi.source.local.isLocal
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Returns a copy of the list with not downloaded episodes removed.
 */
fun List<Episode>.filterDownloaded(anime: Anime): List<Episode> {
    if (anime.isLocal()) return this

    val downloadCache: DownloadCache = Injekt.get()

    return filter {
        downloadCache.isEpisodeDownloaded(
            it.name,
            it.scanlator,
            // AM (CUSTOM_INFORMATION) -->
            anime.ogTitle,
            // <-- AM (CUSTOM_INFORMATION)
            anime.source,
            false,
        )
    }
}
