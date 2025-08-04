package eu.kanade.tachiyomi.ui.reader.viewer

import eu.kanade.tachiyomi.data.database.models.toDomainEpisode
import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
import tachiyomi.domain.episode.service.calculateEpisodeGap as domainCalculateChapterGap

fun calculateChapterGap(higherReaderChapter: ReaderChapter?, lowerReaderChapter: ReaderChapter?): Int {
    return domainCalculateChapterGap(
        higherReaderChapter?.episode?.toDomainEpisode(),
        lowerReaderChapter?.episode?.toDomainEpisode(),
    )
}
