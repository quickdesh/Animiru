package eu.kanade.domain.chapter.model

import eu.kanade.tachiyomi.data.database.models.ChapterImpl
import eu.kanade.tachiyomi.animesource.model.SEpisode
import tachiyomi.domain.episode.model.Episode
import eu.kanade.tachiyomi.data.database.models.Chapter as DbChapter

// TODO: Remove when all deps are migrated
fun Episode.toSChapter(): SEpisode {
    return SEpisode.create().also {
        it.url = url
        it.name = name
        it.date_upload = dateUpload
        it.episode_number = episodeNumber.toFloat()
        it.scanlator = scanlator
    }
}

fun Episode.copyFromSChapter(sEpisode: SEpisode): Episode {
    return this.copy(
        name = sEpisode.name,
        url = sEpisode.url,
        dateUpload = sEpisode.date_upload,
        episodeNumber = sEpisode.episode_number.toDouble(),
        scanlator = sEpisode.scanlator?.ifBlank { null }?.trim(),
    )
}

fun Episode.toDbChapter(): DbChapter = ChapterImpl().also {
    it.id = id
    it.manga_id = animeId
    it.url = url
    it.name = name
    it.scanlator = scanlator
    it.read = seen
    it.bookmark = bookmark
    it.last_page_read = lastSecondSeen.toInt()
    it.date_fetch = dateFetch
    it.date_upload = dateUpload
    it.episode_number = episodeNumber.toFloat()
    it.source_order = sourceOrder.toInt()
}
