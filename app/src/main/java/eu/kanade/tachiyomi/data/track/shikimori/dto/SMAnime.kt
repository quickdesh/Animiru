package eu.kanade.tachiyomi.data.track.shikimori.dto

import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.data.track.shikimori.ShikimoriApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SMSearchResult(
    val data: SMAnimeResults,
)

@Serializable
data class SMAnimeResults(
    val animes: List<SMAnime>,
)

@Serializable
data class SMAnime(
    val id: Long,
    val name: String,
    val episodes: Long,
    val score: Double?,
    val url: String,
    val status: String?,
    val poster: SMPoster?,
    val airedOn: SMAiredDate?,
    val description: String?,
    val kind: String?,
    val studio: List<SMStudio>?,
) {
    fun toTrack(trackId: Long): TrackSearch {
        return TrackSearch.create(trackId).apply {
            remote_id = this@SMAnime.id
            title = name
            total_episodes = episodes
            cover_url = poster?.mainUrl.orEmpty()
            summary = description.orEmpty()
            score = this@SMAnime.score?.takeIf { it > 0.0 } ?: -1.0
            tracking_url = url
            publishing_status = this@SMAnime.status.orEmpty()
            publishing_type = kind?.replace("one_shot", "oneshot").orEmpty()
            start_date = airedOn?.date.orEmpty()
            authors = studio?.map { it.name }.orEmpty()
        }
    }
}

@Serializable
data class SMPoster(
    val mainUrl: String,
)

@Serializable
data class SMAiredDate(
    val date: String?,
)

@Serializable
data class SMStudio(
    val name: String,
)
