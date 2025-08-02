package eu.kanade.domain.track.interactor

import eu.kanade.domain.track.model.toDbTrack
import eu.kanade.tachiyomi.data.track.EnhancedTracker
import eu.kanade.tachiyomi.data.track.Tracker
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.episode.interactor.GetEpisodesByAnimeId
import tachiyomi.domain.episode.interactor.UpdateEpisode
import tachiyomi.domain.episode.model.toEpisodeUpdate
import tachiyomi.domain.track.interactor.InsertTrack
import tachiyomi.domain.track.model.Track
import kotlin.math.max

class SyncChapterProgressWithTrack(
    private val updateEpisode: UpdateEpisode,
    private val insertTrack: InsertTrack,
    private val getEpisodesByAnimeId: GetEpisodesByAnimeId,
) {

    suspend fun await(
        mangaId: Long,
        remoteTrack: Track,
        tracker: Tracker,
    ) {
        if (tracker !is EnhancedTracker) {
            return
        }

        val sortedChapters = getEpisodesByAnimeId.await(mangaId)
            .sortedBy { it.episodeNumber }
            .filter { it.isRecognizedNumber }

        val chapterUpdates = sortedChapters
            .filter { chapter -> chapter.episodeNumber <= remoteTrack.lastEpisodeSeen && !chapter.seen }
            .map { it.copy(seen = true).toEpisodeUpdate() }

        // only take into account continuous reading
        val localLastRead = sortedChapters.takeWhile { it.seen }.lastOrNull()?.episodeNumber ?: 0F
        val lastRead = max(remoteTrack.lastEpisodeSeen, localLastRead.toDouble())
        val updatedTrack = remoteTrack.copy(lastEpisodeSeen = lastRead)

        try {
            tracker.update(updatedTrack.toDbTrack())
            updateEpisode.awaitAll(chapterUpdates)
            insertTrack.await(updatedTrack)
        } catch (e: Throwable) {
            logcat(LogPriority.WARN, e)
        }
    }
}
