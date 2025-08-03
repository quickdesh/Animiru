package eu.kanade.tachiyomi.util.chapter

import eu.kanade.domain.episode.model.applyFilters
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.ui.manga.ChapterList
import tachiyomi.domain.episode.model.Episode
import tachiyomi.domain.anime.model.Anime

/**
 * Gets next unread chapter with filters and sorting applied
 */
fun List<Episode>.getNextUnread(anime: Anime, downloadManager: DownloadManager): Episode? {
    return applyFilters(anime, downloadManager).let { chapters ->
        if (anime.sortDescending()) {
            chapters.findLast { !it.seen }
        } else {
            chapters.find { !it.seen }
        }
    }
}

/**
 * Gets next unread chapter with filters and sorting applied
 */
fun List<ChapterList.Item>.getNextUnread(anime: Anime): Episode? {
    return applyFilters(anime).let { chapters ->
        if (anime.sortDescending()) {
            chapters.findLast { !it.episode.seen }
        } else {
            chapters.find { !it.episode.seen }
        }
    }?.episode
}
