package tachiyomi.domain.history.model

import java.util.Date

data class History(
    val id: Long,
    val episodeId: Long,
    val seenAt: Date?,
) {
    companion object {
        fun create() = History(
            id = -1L,
            episodeId = -1L,
            seenAt = null,
        )
    }
}
