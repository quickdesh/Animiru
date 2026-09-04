package eu.kanade.tachiyomi.data.track.kitsu.dto

import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.serialization.Serializable

@Serializable
data class KitsuAnime(
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
) {
    fun toTrackSearch(trackId: Long): TrackSearch {
        return TrackSearch.create(trackId).apply {
            remote_id = this@KitsuAnime.id.toLong()
            title = titles.preferred
            total_episodes = episodeCount ?: 0
            cover_url = posterImage.getPosterUrl()
            summary = description["en"] ?: ""
            tracking_url = "https://kitsu.app/anime/$slug"
            score = averageRating ?: -1.0
            publishing_status = when (this@KitsuAnime.status) {
                "TBA" -> "TBA"
                "CURRENT" -> "Releasing"
                else -> this@KitsuAnime.status.lowercase().replaceFirstChar { it.uppercase() }
            }
            publishing_type = if (subtype != "TV" && subtype != "OVA" && subtype != "ONA") {
                subtype.lowercase().replaceFirstChar { it.uppercase() }
            } else {
                subtype
            }
            start_date = startDate ?: ""
            authors = productions.nodes
                .filter { it.role.contains("STUDIO") }
                .map { it.company.name }
        }
    }
}

@Serializable
data class KitsuAnimeTitles(
    val preferred: String,
)

@Serializable
data class KitsuAnimeProductionsData(
    val nodes: List<KitsuAnimeProduction>,
)

@Serializable
data class KitsuAnimeProduction(
    val role: String,
    val company: KitsuAnimeCompany,
)

@Serializable
data class KitsuAnimeCompany(
    val name: String,
)

@Serializable
data class KitsuAnimePosters(
    val views: List<KitsuAnimePoster>,
    val original: KitsuAnimePoster,
) {
    // we only ask for the "small" poster in the query
    fun getPosterUrl(): String = views.firstOrNull()?.url ?: original.url
}

@Serializable
data class KitsuAnimePoster(
    val name: String,
    val url: String,
)
