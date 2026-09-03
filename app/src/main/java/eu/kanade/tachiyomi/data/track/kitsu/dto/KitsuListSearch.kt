package eu.kanade.tachiyomi.data.track.kitsu.dto

import eu.kanade.tachiyomi.data.track.TrackerManager
import eu.kanade.tachiyomi.data.track.kitsu.Kitsu
import eu.kanade.tachiyomi.data.track.kitsu.KitsuApi
import eu.kanade.tachiyomi.data.track.kitsu.KitsuDateHelper
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.serialization.Serializable

@Serializable
data class KitsuListSearchResult(
    val data: List<KitsuListSearchItemData>,
    val included: List<KitsuListSearchItemIncluded> = emptyList(),
) {
    fun firstToTrack(): TrackSearch {
        require(data.isNotEmpty()) { "Missing User data from Kitsu" }
        require(included.isNotEmpty()) { "Missing Anime data from Kitsu" }

        val userData = data[0]
        val userDataAttrs = userData.attributes
        val anime = included[0].attributes

        return TrackSearch.create(TrackerManager.KITSU).apply {
            remote_id = included[0].id
            library_id = userData.id
            title = anime.canonicalTitle
            total_episodes = anime.episodeCount ?: 0
            cover_url = anime.posterImage?.original ?: ""
            summary = anime.synopsis ?: ""
            tracking_url = KitsuApi.animeUrl(remote_id)
            publishing_status = anime.status
            publishing_type = anime.showType ?: ""
            start_date = userDataAttrs.startedAt ?: ""
            started_watching_date = KitsuDateHelper.parse(userDataAttrs.startedAt)
            finished_watching_date = KitsuDateHelper.parse(userDataAttrs.finishedAt)
            status = when (userDataAttrs.status) {
                "current" -> Kitsu.WATCHING
                "completed" -> Kitsu.COMPLETED
                "on_hold" -> Kitsu.ON_HOLD
                "dropped" -> Kitsu.DROPPED
                "planned" -> Kitsu.PLAN_TO_WATCH
                else -> throw Exception("Unknown status")
            }
            score = userDataAttrs.ratingTwenty?.toDouble() ?: 0.0
            last_episode_seen = userDataAttrs.progress.toDouble()
            private = userDataAttrs.private
        }
    }
}

@Serializable
data class KitsuListSearchItemData(
    val id: Long,
    val attributes: KitsuListSearchItemDataAttributes,
)

@Serializable
data class KitsuListSearchItemDataAttributes(
    val status: String,
    val startedAt: String?,
    val finishedAt: String?,
    val ratingTwenty: Int?,
    val progress: Int,
    val private: Boolean,
)

@Serializable
data class KitsuSingleAnime(
    val data: KitsuListSearchItemIncluded,
) {
    fun toTrackSearch(): TrackSearch {
        return TrackSearch.create(TrackerManager.KITSU).apply {
            remote_id = data.id
            title = data.attributes.canonicalTitle
            total_episodes = data.attributes.episodeCount ?: 0
            cover_url = data.attributes.posterImage?.original ?: ""
            summary = data.attributes.synopsis ?: ""
            tracking_url = KitsuApi.animeUrl(remote_id)
            score = data.attributes.averageRating?.toDoubleOrNull() ?: -1.0
            publishing_status = data.attributes.status
            publishing_type = data.attributes.showType ?: ""
            start_date = data.attributes.startDate ?: ""
        }
    }
}

@Serializable
data class KitsuListSearchItemIncluded(
    val id: Long,
    val attributes: KitsuListSearchItemIncludedAttributes,
)

@Serializable
data class KitsuListSearchItemIncludedAttributes(
    val canonicalTitle: String,
    val episodeCount: Long?,
    val showType: String?,
    val posterImage: KitsuSearchItemCover?,
    val synopsis: String?,
    val startDate: String?,
    val status: String,
    val averageRating: String?,
)
