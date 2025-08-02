package eu.kanade.tachiyomi.util.chapter

import tachiyomi.domain.episode.model.Episode

/**
 * Returns a copy of the list with duplicate chapters removed
 */
fun List<Episode>.removeDuplicates(currentEpisode: Episode): List<Episode> {
    return groupBy { it.episodeNumber }
        .map { (_, chapters) ->
            chapters.find { it.id == currentEpisode.id }
                ?: chapters.find { it.scanlator == currentEpisode.scanlator }
                ?: chapters.first()
        }
}
