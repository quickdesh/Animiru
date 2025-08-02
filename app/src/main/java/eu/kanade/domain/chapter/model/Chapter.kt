package eu.kanade.domain.chapter.model

import eu.kanade.tachiyomi.data.database.models.ChapterImpl
import eu.kanade.tachiyomi.animesource.model.SEpisode
import tachiyomi.domain.chapter.model.Chapter
import eu.kanade.tachiyomi.data.database.models.Chapter as DbChapter

// TODO: Remove when all deps are migrated
fun Chapter.toSChapter(): SEpisode {
    return SEpisode.create().also {
        it.url = url
        it.name = name
        it.date_upload = dateUpload
        it.episode_number = chapterNumber.toFloat()
        it.scanlator = scanlator
    }
}

fun Chapter.copyFromSChapter(sEpisode: SEpisode): Chapter {
    return this.copy(
        name = sEpisode.name,
        url = sEpisode.url,
        dateUpload = sEpisode.date_upload,
        chapterNumber = sEpisode.episode_number.toDouble(),
        scanlator = sEpisode.scanlator?.ifBlank { null }?.trim(),
    )
}

fun Chapter.toDbChapter(): DbChapter = ChapterImpl().also {
    it.id = id
    it.manga_id = mangaId
    it.url = url
    it.name = name
    it.scanlator = scanlator
    it.read = read
    it.bookmark = bookmark
    it.last_page_read = lastPageRead.toInt()
    it.date_fetch = dateFetch
    it.date_upload = dateUpload
    it.episode_number = chapterNumber.toFloat()
    it.source_order = sourceOrder.toInt()
}
