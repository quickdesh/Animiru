package eu.kanade.domain.anime.interactor

import eu.kanade.domain.anime.model.hasCustomBackground
import eu.kanade.domain.anime.model.hasCustomCover
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.data.cache.BackgroundCache
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.download.DownloadManager
import tachiyomi.domain.anime.interactor.FetchInterval
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.model.AnimeUpdate
import tachiyomi.domain.anime.repository.AnimeRepository
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.source.local.isLocal
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.time.Instant
import java.time.ZonedDateTime

class UpdateAnime(
    private val animeRepository: AnimeRepository,
    private val fetchInterval: FetchInterval,
) {

    suspend fun await(animeUpdate: AnimeUpdate): Boolean {
        return animeRepository.update(animeUpdate)
    }

    suspend fun awaitAll(animeUpdates: List<AnimeUpdate>): Boolean {
        return animeRepository.updateAll(animeUpdates)
    }

    suspend fun awaitUpdateFromSource(
        localAnime: Anime,
        remoteAnime: SAnime,
        manualFetch: Boolean,
        coverCache: CoverCache = Injekt.get(),
        // AY -->
        backgroundCache: BackgroundCache = Injekt.get(),
        // <-- AY
        libraryPreferences: LibraryPreferences = Injekt.get(),
        downloadManager: DownloadManager = Injekt.get(),
    ): Boolean {
        val remoteTitle = try {
            remoteAnime.title
        } catch (_: UninitializedPropertyAccessException) {
            ""
        }

        // if the anime isn't a favorite (or 'update titles' preference is enabled), set its title from source and update in db
        val title =
            if (remoteTitle.isNotEmpty() && (!localAnime.favorite || libraryPreferences.updateAnimeTitles().get())) {
                remoteTitle
            } else {
                null
            }

        val coverLastModified =
            when {
                // Never refresh covers if the url is empty to avoid "losing" existing covers
                remoteAnime.thumbnail_url.isNullOrEmpty() -> null
                !manualFetch && localAnime.thumbnailUrl == remoteAnime.thumbnail_url -> null
                localAnime.isLocal() -> Instant.now().toEpochMilli()
                localAnime.hasCustomCover(coverCache) -> {
                    coverCache.deleteFromCache(localAnime, false)
                    null
                }
                else -> {
                    coverCache.deleteFromCache(localAnime, false)
                    Instant.now().toEpochMilli()
                }
            }

        // AY -->
        val backgroundLastModified =
            when {
                // Never refresh backgrounds if the url is empty to avoid "losing" existing backgrounds
                remoteAnime.background_url.isNullOrEmpty() -> null
                !manualFetch && localAnime.backgroundUrl == remoteAnime.background_url -> null
                localAnime.isLocal() -> Instant.now().toEpochMilli()
                localAnime.hasCustomBackground(backgroundCache) -> {
                    backgroundCache.deleteFromCache(localAnime, false)
                    null
                }
                else -> {
                    backgroundCache.deleteFromCache(localAnime, false)
                    Instant.now().toEpochMilli()
                }
            }
        // <-- AY

        val thumbnailUrl = remoteAnime.thumbnail_url?.takeIf { it.isNotEmpty() }

        // AY -->
        val backgroundUrl = remoteAnime.background_url?.takeIf { it.isNotEmpty() }
        // <-- AY

        val success = animeRepository.update(
            AnimeUpdate(
                id = localAnime.id,
                title = title,
                coverLastModified = coverLastModified,
                // AY -->
                backgroundLastModified = backgroundLastModified,
                // <-- AY
                author = remoteAnime.author,
                artist = remoteAnime.artist,
                description = remoteAnime.description,
                genre = remoteAnime.getGenres(),
                thumbnailUrl = thumbnailUrl,
                // AY -->
                backgroundUrl = backgroundUrl,
                // <-- AY
                status = remoteAnime.status.toLong(),
                updateStrategy = remoteAnime.update_strategy,
                initialized = true,
            ),
        )
        if (success && title != null) {
            downloadManager.renameAnime(localAnime, title)
        }
        return success
    }

    suspend fun awaitUpdateFetchInterval(
        anime: Anime,
        dateTime: ZonedDateTime = ZonedDateTime.now(),
        window: Pair<Long, Long> = fetchInterval.getWindow(dateTime),
    ): Boolean {
        return animeRepository.update(
            fetchInterval.toAnimeUpdate(anime, dateTime, window),
        )
    }

    suspend fun awaitUpdateLastUpdate(animeId: Long): Boolean {
        return animeRepository.update(AnimeUpdate(id = animeId, lastUpdate = Instant.now().toEpochMilli()))
    }

    suspend fun awaitUpdateCoverLastModified(animeId: Long): Boolean {
        return animeRepository.update(AnimeUpdate(id = animeId, coverLastModified = Instant.now().toEpochMilli()))
    }

    // AY -->
    suspend fun awaitUpdateBackgroundLastModified(animeId: Long): Boolean {
        return animeRepository.update(AnimeUpdate(id = animeId, backgroundLastModified = Instant.now().toEpochMilli()))
    }
    // <-- AY

    suspend fun awaitUpdateFavorite(animeId: Long, favorite: Boolean): Boolean {
        val dateAdded = when (favorite) {
            true -> Instant.now().toEpochMilli()
            false -> 0
        }
        return animeRepository.update(
            AnimeUpdate(id = animeId, favorite = favorite, dateAdded = dateAdded),
        )
    }
}
