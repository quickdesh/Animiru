package eu.kanade.domain.track.model

import tachiyomi.domain.track.model.Track
import eu.kanade.tachiyomi.data.database.models.Track as DbTrack

fun Track.copyPersonalFrom(other: Track): Track {
    return this.copy(
        lastEpisodeSeen = other.lastEpisodeSeen,
        score = other.score,
        status = other.status,
        startDate = other.startDate,
        finishDate = other.finishDate,
        private = other.private,
    )
}

fun Track.toDbTrack(): DbTrack = DbTrack.create(trackerId).also {
    it.id = id
    it.manga_id = animeId
    it.remote_id = remoteId
    it.library_id = libraryId
    it.title = title
    it.last_chapter_read = lastEpisodeSeen
    it.total_chapters = totalEpisodes
    it.status = status
    it.score = score
    it.tracking_url = remoteUrl
    it.started_reading_date = startDate
    it.finished_reading_date = finishDate
    it.private = private
}

fun DbTrack.toDomainTrack(idRequired: Boolean = true): Track? {
    val trackId = id ?: if (!idRequired) -1 else return null
    return Track(
        id = trackId,
        animeId = manga_id,
        trackerId = tracker_id,
        remoteId = remote_id,
        libraryId = library_id,
        title = title,
        lastEpisodeSeen = last_chapter_read,
        totalEpisodes = total_chapters,
        status = status,
        score = score,
        remoteUrl = tracking_url,
        startDate = started_reading_date,
        finishDate = finished_reading_date,
        private = private,
    )
}
