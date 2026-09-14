package eu.kanade.domain.episode.interactor

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.anizip.AniZipService
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.track.TrackerManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import logcat.LogPriority
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.anime.interactor.GetAnime
import tachiyomi.domain.episode.interactor.GetEpisodesByAnimeId
import tachiyomi.domain.episode.interactor.UpdateEpisode
import tachiyomi.domain.episode.model.EpisodeUpdate
import tachiyomi.domain.track.interactor.GetTracks

@Inject
class EnrichEpisodesWithAniZip(
    private val aniZipService: AniZipService,
    private val getTracks: GetTracks,
    private val getEpisodesByAnimeId: GetEpisodesByAnimeId,
    private val updateEpisode: UpdateEpisode,
    private val getAnime: GetAnime,
    private val downloadManager: DownloadManager,
) {
    suspend fun await(animeId: Long, fallbackTrackAnimeId: Long? = null) = withIOContext {
        try {
            val tracks = getTracks.await(animeId).ifEmpty {
                fallbackTrackAnimeId?.let { getTracks.await(it) }.orEmpty()
            }
            if (tracks.isEmpty()) return@withIOContext

            val anilistTrack = tracks.firstOrNull { it.trackerId == TrackerManager.ANILIST && it.remoteId > 0 }
            val malTrack = tracks.firstOrNull { it.trackerId == 1L && it.remoteId > 0 }

            if (anilistTrack == null && malTrack == null) return@withIOContext

            val metadata = anilistTrack?.let {
                aniZipService.getMetadata(anilistId = it.remoteId)
            }.orEmpty().ifEmpty {
                malTrack?.let {
                    aniZipService.getMetadata(malId = it.remoteId)
                }.orEmpty()
            }
            if (metadata.isEmpty()) return@withIOContext

            val episodes = getEpisodesByAnimeId.await(animeId)
            if (episodes.isEmpty()) return@withIOContext

            val anime = getAnime.await(animeId)
            val updates = mutableListOf<EpisodeUpdate>()

            for (episode in episodes) {
                val meta = AniZipService.findMetaForEpisode(metadata, episode.episodeNumber, episode.name)
                    ?: continue

                var changed = false
                var newName = episode.name
                var newPreviewUrl = episode.previewUrl
                var newSummary = episode.summary
                var newDateUpload = episode.dateUpload

                // Check download status to avoid altering persisted name of downloaded episodes
                val isDownloaded = if (anime != null) {
                    downloadManager.isEpisodeDownloaded(
                        episode.name,
                        episode.scanlator,
                        episode.url,
                        anime.ogTitle,
                        anime.source,
                    )
                } else {
                    false
                }

                // 1. Title enrichment
                val metaTitle = meta.title
                if (!metaTitle.isNullOrBlank()) {
                    if (!isDownloaded && !episode.name.contains(metaTitle, ignoreCase = true)) {
                        newName = "${episode.name} - $metaTitle"
                        changed = true
                    }
                }

                // 2. Thumbnail / Preview URL
                if (newPreviewUrl.isNullOrBlank() && !meta.image.isNullOrBlank()) {
                    newPreviewUrl = meta.image
                    changed = true
                }

                // 3. Summary / Overview
                if (newSummary.isNullOrBlank() && !meta.overview.isNullOrBlank()) {
                    newSummary = meta.overview
                    changed = true
                }

                // 4. Air Date (if not set on episode)
                if (newDateUpload <= 0L && meta.airDateMillis != null && meta.airDateMillis > 0L) {
                    newDateUpload = meta.airDateMillis
                    changed = true
                }

                // 5. Rating and extra info in memo
                val currentRating = episode.memo["rating"]?.jsonPrimitive?.contentOrNull
                val currentAirDate = episode.memo["airDate"]?.jsonPrimitive?.contentOrNull
                val currentTitle = episode.memo["anizip_title"]?.jsonPrimitive?.contentOrNull

                val hasRatingUpdate = !meta.rating.isNullOrBlank() && currentRating != meta.rating
                val hasAirDateUpdate = !meta.airDate.isNullOrBlank() && currentAirDate != meta.airDate
                val hasTitleUpdate = !metaTitle.isNullOrBlank() && currentTitle != metaTitle

                val newMemo = if (hasRatingUpdate || hasAirDateUpdate || hasTitleUpdate) {
                    changed = true
                    buildJsonObject {
                        episode.memo.forEach { (k, v) -> put(k, v) }
                        meta.rating?.let { put("rating", it) }
                        meta.airDate?.let { put("airDate", it) }
                        metaTitle?.let { put("anizip_title", it) }
                    }
                } else {
                    episode.memo
                }

                if (changed) {
                    updates.add(
                        EpisodeUpdate(
                            id = episode.id,
                            name = newName,
                            previewUrl = newPreviewUrl,
                            summary = newSummary,
                            dateUpload = newDateUpload,
                            memo = newMemo,
                        ),
                    )
                }
            }

            if (updates.isNotEmpty()) {
                logcat(LogPriority.INFO) { "AniZip: Enriching ${updates.size} episodes for anime $animeId" }
                updateEpisode.awaitAll(updates)
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "AniZip: Failed to enrich episodes for anime $animeId" }
        }
    }
}
