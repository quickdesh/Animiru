package eu.kanade.tachiyomi.util.episode

import tachiyomi.domain.episode.model.Episode

/**
 * Returns a copy of the list with duplicate episodes removed
 */
fun List<Episode>.removeDuplicates(currentEpisode: Episode): List<Episode> {
    return groupBy { it.episodeNumber }
        .map { (_, episodes) ->
            episodes.find { it.id == currentEpisode.id }
                ?: episodes.find { it.scanlator == currentEpisode.scanlator }
                ?: episodes.first()
        }
}
