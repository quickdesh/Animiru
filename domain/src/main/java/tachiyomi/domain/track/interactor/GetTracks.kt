package tachiyomi.domain.track.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.track.model.Track
import tachiyomi.domain.track.repository.TrackRepository

@Inject
class GetTracks(
    private val trackRepository: TrackRepository,
) {

    suspend fun awaitOne(id: Long): Track? {
        return try {
            trackRepository.getTrackById(id)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            null
        }
    }

    suspend fun await(animeId: Long): List<Track> {
        return try {
            trackRepository.getTracksByAnimeId(animeId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            emptyList()
        }
    }

    fun subscribe(animeId: Long): Flow<List<Track>> {
        return trackRepository.getTracksByAnimeIdAsFlow(animeId)
    }

    // AM (GROUPING) -->
    suspend fun await(): List<Track> {
        return try {
            trackRepository.getTracks()
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            emptyList()
        }
    }
    // <-- AM (GROUPING)
}
