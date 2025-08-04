package eu.kanade.tachiyomi.data.backup.create.creators

import eu.kanade.tachiyomi.data.backup.create.BackupOptions
import eu.kanade.tachiyomi.data.backup.models.BackupEpisode
import eu.kanade.tachiyomi.data.backup.models.BackupHistory
import eu.kanade.tachiyomi.data.backup.models.BackupAnime
import eu.kanade.tachiyomi.data.backup.models.backupEpisodeMapper
import eu.kanade.tachiyomi.data.backup.models.backupTrackMapper
import tachiyomi.data.DatabaseHandler
import tachiyomi.domain.anime.interactor.GetCustomAnimeInfo
import tachiyomi.domain.category.interactor.GetCategories
import tachiyomi.domain.history.interactor.GetHistory
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.model.CustomAnimeInfo
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnimeBackupCreator(
    private val handler: DatabaseHandler = Injekt.get(),
    private val getCategories: GetCategories = Injekt.get(),
    private val getHistory: GetHistory = Injekt.get(),
    // AM (CUSTOM_INFORMATION) -->
    private val getCustomAnimeInfo: GetCustomAnimeInfo = Injekt.get(),
    // <-- AM (CUSTOM_INFORMATION)
) {

    suspend operator fun invoke(animes: List<Anime>, options: BackupOptions): List<BackupAnime> {
        return animes.map {
            backupAnime(it, options)
        }
    }

    private suspend fun backupAnime(anime: Anime, options: BackupOptions): BackupAnime {
        // Entry for this anime
        val animeObject = anime.toBackupAnime(
            // AM (CUSTOM_INFORMATION) -->
            if (options.customInfo) getCustomAnimeInfo.get(anime.id) else null,
            // <-- AM (CUSTOM_INFORMATION)
        )

        animeObject.excludedScanlators = handler.awaitList {
            excluded_scanlatorsQueries.getExcludedScanlatorsByAnimeId(anime.id)
        }

        if (options.episodes) {
            // Backup all the episodes
            handler.awaitList {
                episodesQueries.getEpisodesByAnimeId(
                    animeId = anime.id,
                    applyScanlatorFilter = 0, // false
                    mapper = backupEpisodeMapper,
                )
            }
                .takeUnless(List<BackupEpisode>::isEmpty)
                ?.let { animeObject.episodes = it }
        }

        if (options.categories) {
            // Backup categories for this anime
            val categoriesForAnime = getCategories.await(anime.id)
            if (categoriesForAnime.isNotEmpty()) {
                animeObject.categories = categoriesForAnime.map { it.order }
            }
        }

        if (options.tracking) {
            val tracks = handler.awaitList { anime_syncQueries.getTracksByAnimeId(anime.id, backupTrackMapper) }
            if (tracks.isNotEmpty()) {
                animeObject.tracking = tracks
            }
        }

        if (options.history) {
            val historyByAnimeId = getHistory.await(anime.id)
            if (historyByAnimeId.isNotEmpty()) {
                val history = historyByAnimeId.map { history ->
                    val episode = handler.awaitOne { episodesQueries.getEpisodeById(history.episodeId) }
                    BackupHistory(episode.url, history.seenAt?.time ?: 0L)
                }
                if (history.isNotEmpty()) {
                    animeObject.history = history
                }
            }
        }

        return animeObject
    }
}

private fun Anime.toBackupAnime(
    // AM (CUSTOM_INFORMATION) -->
    customAnimeInfo: CustomAnimeInfo?
    // <-- AM (CUSTOM_INFORMATION)
) =
    BackupAnime(
        url = this.url,
        // AM (CUSTOM_INFORMATION) -->
        title = this.ogTitle,
        artist = this.ogArtist,
        author = this.ogAuthor,
        description = this.ogDescription,
        genre = this.ogGenre.orEmpty(),
        status = this.ogStatus.toInt(),
        // <-- AM (CUSTOM_INFORMATION)
        thumbnailUrl = this.thumbnailUrl,
        favorite = this.favorite,
        source = this.source,
        dateAdded = this.dateAdded,
        viewer_flags = this.viewerFlags.toInt(),
        episodeFlags = this.episodeFlags.toInt(),
        updateStrategy = this.updateStrategy,
        lastModifiedAt = this.lastModifiedAt,
        favoriteModifiedAt = this.favoriteModifiedAt,
        version = this.version,
        notes = this.notes,
    )
        // AM (CUSTOM_INFORMATION) -->
        .also { backupAnime ->
            customAnimeInfo?.let {
                backupAnime.customTitle = it.title
                backupAnime.customArtist = it.artist
                backupAnime.customAuthor = it.author
                backupAnime.customDescription = it.description
                backupAnime.customGenre = it.genre
                backupAnime.customStatus = it.status?.toInt() ?: 0
            }
        }
        // <-- AM (CUSTOM_INFORMATION)
