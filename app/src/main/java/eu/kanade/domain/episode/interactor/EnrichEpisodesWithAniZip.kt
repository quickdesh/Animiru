package eu.kanade.domain.episode.interactor

import dev.zacsweers.metro.Inject
import eu.kanade.domain.track.service.TrackPreferences
import eu.kanade.tachiyomi.data.anizip.AniZipService
import eu.kanade.tachiyomi.data.anizip.model.AniZipEpisodeMeta
import eu.kanade.tachiyomi.data.track.TrackerManager
import logcat.LogPriority
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.episode.interactor.GetEpisodesByAnimeId
import tachiyomi.domain.track.interactor.GetTracks

@Inject
class EnrichEpisodesWithAniZip(
    private val aniZipService: AniZipService,
    private val getTracks: GetTracks,
    private val getEpisodesByAnimeId: GetEpisodesByAnimeId,
    private val trackPreferences: TrackPreferences,
) {
    suspend fun await(animeId: Long, fallbackTrackAnimeId: Long? = null): Map<Long, AniZipEpisodeMeta> = withIOContext {
        if (!trackPreferences.enableAniZip.get()) return@withIOContext emptyMap()

        try {
            val tracks = getTracks.await(animeId).ifEmpty {
                fallbackTrackAnimeId?.let { getTracks.await(it) }.orEmpty()
            }
            if (tracks.isEmpty()) return@withIOContext emptyMap()

            val anilistTrack = tracks.firstOrNull { it.trackerId == TrackerManager.ANILIST && it.remoteId > 0 }
            val malTrack = tracks.firstOrNull { it.trackerId == 1L && it.remoteId > 0 }

            if (anilistTrack == null && malTrack == null) return@withIOContext emptyMap()

            val metadata = anilistTrack?.let {
                aniZipService.getMetadata(anilistId = it.remoteId)
            }.orEmpty().ifEmpty {
                malTrack?.let {
                    aniZipService.getMetadata(malId = it.remoteId)
                }.orEmpty()
            }
            if (metadata.isEmpty()) return@withIOContext emptyMap()

            val episodes = getEpisodesByAnimeId.await(animeId)
            if (episodes.isEmpty()) return@withIOContext emptyMap()

            val resultMap = mutableMapOf<Long, AniZipEpisodeMeta>()

            for (episode in episodes) {
                val meta = aniZipService.findMetaForEpisode(metadata, episode.episodeNumber, episode.name)
                    ?: continue
                resultMap[episode.id] = meta
            }

            resultMap
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "AniZip: Failed to enrich episodes for anime $animeId" }
            emptyMap()
        }
    }
}
