package tachiyomi.domain.episode.service

import tachiyomi.domain.episode.model.Episode
import kotlin.math.floor

fun List<Double>.missingEntriesCount(): Int {
    if (this.isEmpty()) {
        return 0
    }

    val entries = this
        // Ignore unknown entry numbers
        .filterNot { it == -1.0 }
        // Convert to integers, as we cannot check if 16.5 is missing
        .map(Double::toInt)
        // Only keep unique entries so that -1 or 16 are not counted multiple times
        .distinct()
        .sorted()

    if (entries.isEmpty()) {
        return 0
    }

    var missingEntriesCount = 0
    var previousEntry = 0 // The actual entry number, not the array index

    // We go from 0 to lastEntry - Make sure to use the current index instead of the value
    for (i in entries.indices) {
        val currentEntry = entries[i]
        if (currentEntry > previousEntry + 1) {
            // Add the amount of missing entries
            missingEntriesCount += currentEntry - previousEntry - 1
        }
        previousEntry = currentEntry
    }

    return missingEntriesCount
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
