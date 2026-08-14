package eu.kanade.tachiyomi.data.track.hikka.dto

import kotlinx.serialization.Serializable

@Serializable
data class HKAnimePagination(
    val pagination: HKPagination,
    val list: List<HKAnime>,
)
