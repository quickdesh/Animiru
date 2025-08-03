package eu.kanade.domain.episode.interactor

import eu.kanade.domain.download.interactor.DeleteDownload
import logcat.LogPriority
import tachiyomi.core.common.util.lang.withNonCancellableContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.episode.model.Episode
import tachiyomi.domain.episode.model.EpisodeUpdate
import tachiyomi.domain.episode.repository.EpisodeRepository
import tachiyomi.domain.download.service.DownloadPreferences
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.repository.AnimeRepository

class SetReadStatus(
    private val downloadPreferences: DownloadPreferences,
    private val deleteDownload: DeleteDownload,
    private val animeRepository: AnimeRepository,
    private val episodeRepository: EpisodeRepository,
) {

    private val mapper = { episode: Episode, read: Boolean ->
        EpisodeUpdate(
            seen = read,
            lastSecondSeen = if (!read) 0 else null,
            id = episode.id,
        )
    }

    suspend fun await(read: Boolean, vararg episodes: Episode): Result = withNonCancellableContext {
        val chaptersToUpdate = episodes.filter {
            when (read) {
                true -> !it.seen
                false -> it.seen || it.lastSecondSeen > 0
            }
        }
        if (chaptersToUpdate.isEmpty()) {
            return@withNonCancellableContext Result.NoChapters
        }

        try {
            episodeRepository.updateAll(
                chaptersToUpdate.map { mapper(it, read) },
            )
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            return@withNonCancellableContext Result.InternalError(e)
        }

        if (read && downloadPreferences.removeAfterMarkedAsSeen().get()) {
            chaptersToUpdate
                .groupBy { it.animeId }
                .forEach { (mangaId, chapters) ->
                    deleteDownload.awaitAll(
                        anime = animeRepository.getAnimeById(mangaId),
                        episodes = chapters.toTypedArray(),
                    )
                }
        }

        Result.Success
    }

    suspend fun await(mangaId: Long, read: Boolean): Result = withNonCancellableContext {
        await(
            read = read,
            episodes = episodeRepository
                .getEpisodeByAnimeId(mangaId)
                .toTypedArray(),
        )
    }

    suspend fun await(anime: Anime, read: Boolean) =
        await(anime.id, read)

    sealed interface Result {
        data object Success : Result
        data object NoChapters : Result
        data class InternalError(val error: Throwable) : Result
    }
}
