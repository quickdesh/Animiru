package eu.kanade.domain.episode.model

import eu.kanade.tachiyomi.data.database.models.EpisodeImpl
import eu.kanade.tachiyomi.animesource.model.SEpisode
import tachiyomi.domain.episode.model.Episode
import eu.kanade.tachiyomi.data.database.models.Episode as DbChapter

// TODO: Remove when all deps are migrated
fun Episode.toSEpisode(): SEpisode {
    return SEpisode.create().also {
        it.url = url
        it.name = name
        it.date_upload = dateUpload
        it.episode_number = episodeNumber.toFloat()
        it.scanlator = scanlator
    }
}

fun Episode.copyFromSEpisode(sEpisode: SEpisode): Episode {
    return this.copy(
        name = sEpisode.name,
        url = sEpisode.url,
        dateUpload = sEpisode.date_upload,
        episodeNumber = sEpisode.episode_number.toDouble(),
        scanlator = sEpisode.scanlator?.ifBlank { null }?.trim(),
    )
}

fun Episode.toDbEpisode(): DbChapter = EpisodeImpl().also {
    it.id = id
    it.anime_id = animeId
    it.url = url
    it.name = name
    it.scanlator = scanlator
    it.seen = seen
    it.bookmark = bookmark
    // AM (FILLERMARK) -->
    it.fillermark = fillermark
    // <-- AM (FILLERMARK)
    it.last_second_seen = lastSecondSeen
    it.date_fetch = dateFetch
    it.date_upload = dateUpload
    it.episode_number = episodeNumber.toFloat()
    it.source_order = sourceOrder.toInt()
    // AM (SYNC) -->
    it.last_modified = lastModifiedAt
    it.version = version
    // <-- AM (SYNC)
}
