package eu.kanade.presentation.history.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import tachiyomi.domain.history.model.HistoryWithRelations
import java.util.Date

internal class HistoryWithRelationsProvider : PreviewParameterProvider<HistoryWithRelations> {

    private val simple = HistoryWithRelations(
        id = 1L,
        episodeId = 2L,
        animeId = 3L,
        title = "Test Title",
        episodeNumber = 10.2,
        seenAt = Date(1697247357L),
        readDuration = 123L,
        coverData = tachiyomi.domain.anime.model.AnimeCover(
            animeId = 3L,
            sourceId = 4L,
            isAnimeFavorite = false,
            url = "https://example.com/cover.png",
            lastModified = 5L,
        ),
    )

    private val historyWithoutReadAt = HistoryWithRelations(
        id = 1L,
        episodeId = 2L,
        animeId = 3L,
        title = "Test Title",
        episodeNumber = 10.2,
        seenAt = null,
        readDuration = 123L,
        coverData = tachiyomi.domain.anime.model.AnimeCover(
            animeId = 3L,
            sourceId = 4L,
            isAnimeFavorite = false,
            url = "https://example.com/cover.png",
            lastModified = 5L,
        ),
    )

    private val historyWithNegativeChapterNumber = HistoryWithRelations(
        id = 1L,
        episodeId = 2L,
        animeId = 3L,
        title = "Test Title",
        episodeNumber = -2.0,
        seenAt = Date(1697247357L),
        readDuration = 123L,
        coverData = tachiyomi.domain.anime.model.AnimeCover(
            animeId = 3L,
            sourceId = 4L,
            isAnimeFavorite = false,
            url = "https://example.com/cover.png",
            lastModified = 5L,
        ),
    )

    override val values: Sequence<HistoryWithRelations>
        get() = sequenceOf(simple, historyWithoutReadAt, historyWithNegativeChapterNumber)
}
