package eu.kanade.tachiyomi.data.track.anilist.dto

import kotlinx.serialization.Serializable

@Serializable
data class ALSearchItem(
    val id: Long,
    val title: ALItemTitle,
    val coverImage: ItemCover,
    val description: String?,
    val format: String?,
    val status: String?,
    val startDate: ALFuzzyDate,
    val episodes: Long?,
    val averageScore: Int?,
    // AY -->
    val studios: ALStudios?,
    // <-- AY
) {
    fun toALAnime(): ALAnime = ALAnime(
        remoteId = id,
        title = title.userPreferred,
        imageUrl = coverImage.large,
        description = description,
        format = format?.replace("_", "-") ?: "",
        publishingStatus = status ?: "",
        startDateFuzzy = startDate.toEpochMilli(),
        totalEpisodes = episodes ?: 0,
        averageScore = averageScore ?: -1,
        // AY -->
        studios = studios!!,
        // <-- AY
    )
}

@Serializable
data class ALItemTitle(
    val userPreferred: String,
)

@Serializable
data class ItemCover(
    val large: String,
)

// AY -->
@Serializable
data class ALStudios(
    val edges: List<ALStudiosEdge>,
)

@Serializable
data class ALStudiosEdge(
    val isMain: Boolean,
    val node: ALStudiosNode,
)

@Serializable
data class ALStudiosNode(
    val name: String,
)
// <-- AY
