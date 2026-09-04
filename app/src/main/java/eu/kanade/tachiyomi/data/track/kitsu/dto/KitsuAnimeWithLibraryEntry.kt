package eu.kanade.tachiyomi.data.track.kitsu.dto

import eu.kanade.tachiyomi.data.track.kitsu.toKitsuLocalStatus
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.serialization.Serializable
import kotlin.time.Instant

// KitsuAnime extended with KitsuLibraryEntryData
@Serializable
data class KitsuAnimeWithLibraryEntry(
    val id: String,
    val titles: KitsuAnimeTitles,
    val episodeCount: Long?,
    val productions: KitsuAnimeProductionsData,
    val posterImage: KitsuAnimePosters,
    val description: Map<String, String>,
    val status: String,
    val subtype: String,
    val startDate: String?,
    val endDate: String?,
    val slug: String,
    val averageRating: Double?,
    val myLibraryEntry: KitsuLibraryEntryData?,
) {
    fun toTrackSearch(trackId: Long): TrackSearch? {
        if (myLibraryEntry == null) return null

        return TrackSearch.create(trackId).apply {
            remote_id = this@KitsuAnimeWithLibraryEntry.id.toLong()
            library_id = myLibraryEntry.id.toLong()
            title = titles.preferred
            total_episodes = episodeCount ?: 0
            cover_url = posterImage.getPosterUrl()
            summary = description["en"] ?: ""
            tracking_url = "https://kitsu.app/anime/$slug"
            publishing_status = when (this@KitsuAnimeWithLibraryEntry.status) {
                "TBA" -> "TBA"
                "CURRENT" -> "Releasing"
                else -> this@KitsuAnimeWithLibraryEntry.status.lowercase().replaceFirstChar { it.uppercase() }
            }
            publishing_type = if (subtype != "OEL") {
                subtype.lowercase().replaceFirstChar { it.uppercase() }
            } else {
                subtype
            }
            start_date = startDate ?: ""
            authors = productions.nodes
                .filter { it.role.contains("STUDIO") }
                .map { it.company.name }

            started_watching_date = myLibraryEntry.startedAt?.let { Instant.parse(it).toEpochMilliseconds() } ?: 0
            finished_watching_date = myLibraryEntry.finishedAt?.let { Instant.parse(it).toEpochMilliseconds() } ?: 0
            status = myLibraryEntry.status.toKitsuLocalStatus()
            score = myLibraryEntry.rating?.toDouble() ?: 0.0
            last_episode_seen = myLibraryEntry.progress.toDouble()
            private = myLibraryEntry.private
        }
    }
}

@Serializable
data class KitsuLibraryEntryData(
    val id: String,
    val private: Boolean,
    val progress: Long,
    val rating: Long?,
    val reconsuming: Boolean,
    val status: String,
    val startedAt: String?,
    val finishedAt: String?,
)
