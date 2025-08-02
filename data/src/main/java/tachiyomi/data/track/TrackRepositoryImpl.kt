package tachiyomi.data.track

import kotlinx.coroutines.flow.Flow
import tachiyomi.data.DatabaseHandler
import tachiyomi.domain.track.model.Track
import tachiyomi.domain.track.repository.TrackRepository

class TrackRepositoryImpl(
    private val handler: DatabaseHandler,
) : TrackRepository {

    override suspend fun getTrackById(id: Long): Track? {
        return handler.awaitOneOrNull { manga_syncQueries.getTrackById(id, TrackMapper::mapTrack) }
    }

    override suspend fun getTracksByAnimeId(animeId: Long): List<Track> {
        return handler.awaitList {
            manga_syncQueries.getTracksByMangaId(animeId, TrackMapper::mapTrack)
        }
    }

    override fun getTracksAsFlow(): Flow<List<Track>> {
        return handler.subscribeToList {
            manga_syncQueries.getTracks(TrackMapper::mapTrack)
        }
    }

    override fun getTracksByAnimeIdAsFlow(animeId: Long): Flow<List<Track>> {
        return handler.subscribeToList {
            manga_syncQueries.getTracksByMangaId(animeId, TrackMapper::mapTrack)
        }
    }

    override suspend fun delete(animeId: Long, trackerId: Long) {
        handler.await {
            manga_syncQueries.delete(
                mangaId = animeId,
                syncId = trackerId,
            )
        }
    }

    override suspend fun insert(track: Track) {
        insertValues(track)
    }

    override suspend fun insertAll(tracks: List<Track>) {
        insertValues(*tracks.toTypedArray())
    }

    private suspend fun insertValues(vararg tracks: Track) {
        handler.await(inTransaction = true) {
            tracks.forEach { mangaTrack ->
                manga_syncQueries.insert(
                    mangaId = mangaTrack.animeId,
                    syncId = mangaTrack.trackerId,
                    remoteId = mangaTrack.remoteId,
                    libraryId = mangaTrack.libraryId,
                    title = mangaTrack.title,
                    lastChapterRead = mangaTrack.lastEpisodeSeen,
                    totalChapters = mangaTrack.totalEpisodes,
                    status = mangaTrack.status,
                    score = mangaTrack.score,
                    remoteUrl = mangaTrack.remoteUrl,
                    startDate = mangaTrack.startDate,
                    finishDate = mangaTrack.finishDate,
                    private = mangaTrack.private,
                )
            }
        }
    }
}
