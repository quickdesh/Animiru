package tachiyomi.domain.episode.service

import tachiyomi.domain.episode.model.Episode
import kotlin.math.floor

fun List<Double>.missingEpisodesCount(): Int {
    if (this.isEmpty()) {
        return 0
    }

    val episodes = this
        // Ignore unknown episode numbers
        .filterNot { it == -1.0 }
        // Convert to integers, as we cannot check if 16.5 is missing
        .map(Double::toInt)
        // Only keep unique episodes so that -1 or 16 are not counted multiple times
        .distinct()
        .sorted()

    if (episodes.isEmpty()) {
        return 0
    }

    var missingEpisodesCount = 0
    var previousEpisode = 0 // The actual episode number, not the array index

    // We go from 0 to lastEpisode - Make sure to use the current index instead of the value
    for (i in episodes.indices) {
        val currentEpisode = episodes[i]
        if (currentEpisode > previousEpisode + 1) {
            // Add the amount of missing episodes
            missingEpisodesCount += currentEpisode - previousEpisode - 1
        }
        previousEpisode = currentEpisode
    }

    return missingEpisodesCount
}

fun calculateEpisodeGap(higherEpisode: Episode?, lowerEpisode: Episode?): Int {
    if (higherEpisode == null || lowerEpisode == null) return 0
    if (!higherEpisode.isRecognizedNumber || !lowerEpisode.isRecognizedNumber) return 0
    return calculateEpisodeGap(higherEpisode.episodeNumber, lowerEpisode.episodeNumber)
}

fun calculateEpisodeGap(higherEpisodeNumber: Double, lowerEpisodeNumber: Double): Int {
    if (higherEpisodeNumber < 0.0 || lowerEpisodeNumber < 0.0) return 0
    return floor(higherEpisodeNumber).toInt() - floor(lowerEpisodeNumber).toInt() - 1
}
