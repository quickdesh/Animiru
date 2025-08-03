package tachiyomi.data.track

import tachiyomi.domain.track.model.Track

object TrackMapper {
    fun mapTrack(
        id: Long,
        animeId: Long,
        syncId: Long,
        remoteId: Long,
        libraryId: Long?,
        title: String,
        lastEpisodeSeen: Double,
        totalEpisodes: Long,
        status: Long,
        score: Double,
        remoteUrl: String,
        startDate: Long,
        finishDate: Long,
        private: Boolean,
    ): Track = Track(
        id = id,
        animeId = animeId,
        trackerId = syncId,
        remoteId = remoteId,
        libraryId = libraryId,
        title = title,
        lastEpisodeSeen = lastEpisodeSeen,
        totalEpisodes = totalEpisodes,
        status = status,
        score = score,
        remoteUrl = remoteUrl,
        startDate = startDate,
        finishDate = finishDate,
        private = private,
    )
}
