package tachiyomi.domain.history.model

import java.util.Date

data class HistoryUpdate(
    val episodeId: Long,
    val seenAt: Date,
)
