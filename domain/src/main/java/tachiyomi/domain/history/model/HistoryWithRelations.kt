package tachiyomi.domain.history.model

import tachiyomi.domain.anime.model.AnimeCover
import java.util.Date

data class HistoryWithRelations(
    val id: Long,
    val episodeId: Long,
    val animeId: Long,
    val title: String,
    val episodeNumber: Double,
    val seenAt: Date?,
    val coverData: AnimeCover,
)
