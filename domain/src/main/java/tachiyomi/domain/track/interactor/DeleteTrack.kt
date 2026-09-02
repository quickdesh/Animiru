package tachiyomi.domain.track.interactor

import dev.zacsweers.metro.Inject
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.track.repository.TrackRepository

@Inject
class DeleteTrack(
    private val trackRepository: TrackRepository,
) {

    suspend fun await(animeId: Long, trackerId: Long) {
        try {
            trackRepository.delete(animeId, trackerId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
        }
    }
}
