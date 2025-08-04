package eu.kanade.tachiyomi.data.track.kitsu.dto

import kotlinx.serialization.Serializable

@Serializable
data class KitsuAddAnimeResult(
    val data: KitsuAddAnimeItem,
)

@Serializable
data class KitsuAddAnimeItem(
    val id: Long,
)
