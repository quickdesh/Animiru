package eu.kanade.tachiyomi.data.track.anilist.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ALAddAnimeResult(
    val data: ALAddAnimeData,
)

@Serializable
data class ALAddAnimeData(
    @SerialName("SaveMediaListEntry")
    val entry: ALAddAnimeEntry,
)

@Serializable
data class ALAddAnimeEntry(
    val id: Long,
)
