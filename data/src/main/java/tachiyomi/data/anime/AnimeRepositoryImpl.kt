package tachiyomi.data.anime

import kotlinx.coroutines.flow.Flow
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.data.DatabaseHandler
import tachiyomi.data.StringListColumnAdapter
import tachiyomi.data.UpdateStrategyColumnAdapter
import tachiyomi.domain.library.model.LibraryAnime
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.model.AnimeUpdate
import tachiyomi.domain.anime.model.AnimeWithEpisodeCount
import tachiyomi.domain.anime.repository.AnimeRepository
import java.time.LocalDate
import java.time.ZoneId

class AnimeRepositoryImpl(
    private val handler: DatabaseHandler,
) : AnimeRepository {

    override suspend fun getAnimeById(id: Long): Anime {
        return handler.awaitOne { animesQueries.getAnimeById(id, AnimeMapper::mapAnime) }
    }

    override suspend fun getAnimeByIdAsFlow(id: Long): Flow<Anime> {
        return handler.subscribeToOne { animesQueries.getAnimeById(id, AnimeMapper::mapAnime) }
    }

    override suspend fun getAnimeByUrlAndSourceId(url: String, sourceId: Long): Anime? {
        return handler.awaitOneOrNull {
            animesQueries.getAnimeByUrlAndSource(
                url,
                sourceId,
                AnimeMapper::mapAnime,
            )
        }
    }

    override fun getAnimeByUrlAndSourceIdAsFlow(url: String, sourceId: Long): Flow<Anime?> {
        return handler.subscribeToOneOrNull {
            animesQueries.getAnimeByUrlAndSource(
                url,
                sourceId,
                AnimeMapper::mapAnime,
            )
        }
    }

    override suspend fun getFavorites(): List<Anime> {
        return handler.awaitList { animesQueries.getFavorites(AnimeMapper::mapAnime) }
    }

    override suspend fun getSeenAnimeNotInLibrary(): List<Anime> {
        return handler.awaitList { animesQueries.getSeenAnimeNotInLibrary(AnimeMapper::mapAnime) }
    }

    override suspend fun getLibraryAnime(): List<LibraryAnime> {
        return handler.awaitList { libraryViewQueries.library(AnimeMapper::mapLibraryAnime) }
    }

    override fun getLibraryAnimeAsFlow(): Flow<List<LibraryAnime>> {
        return handler.subscribeToList { libraryViewQueries.library(AnimeMapper::mapLibraryAnime) }
    }

    override fun getFavoritesBySourceId(sourceId: Long): Flow<List<Anime>> {
        return handler.subscribeToList { animesQueries.getFavoriteBySourceId(sourceId, AnimeMapper::mapAnime) }
    }

    override suspend fun getDuplicateLibraryAnime(id: Long, title: String): List<AnimeWithEpisodeCount> {
        return handler.awaitList {
            animesQueries.getDuplicateLibraryAnime(id, title, AnimeMapper::mapAnimeWithEpisodeCount)
        }
    }

    override suspend fun getUpcomingAnime(statuses: Set<Long>): Flow<List<Anime>> {
        val epochMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        return handler.subscribeToList {
            animesQueries.getUpcomingAnime(epochMillis, statuses, AnimeMapper::mapAnime)
        }
    }

    override suspend fun resetViewerFlags(): Boolean {
        return try {
            handler.await { animesQueries.resetViewerFlags() }
            true
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            false
        }
    }

    override suspend fun setAnimeCategories(animeId: Long, categoryIds: List<Long>) {
        handler.await(inTransaction = true) {
            animes_categoriesQueries.deleteAnimeCategoryByAnimeId(animeId)
            categoryIds.map { categoryId ->
                animes_categoriesQueries.insert(animeId, categoryId)
            }
        }
    }

    override suspend fun update(update: AnimeUpdate): Boolean {
        return try {
            partialUpdate(update)
            true
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            false
        }
    }

    override suspend fun updateAll(animeUpdates: List<AnimeUpdate>): Boolean {
        return try {
            partialUpdate(*animeUpdates.toTypedArray())
            true
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            false
        }
    }

    override suspend fun insertNetworkAnime(anime: List<Anime>): List<Anime> {
        return handler.await(inTransaction = true) {
            anime.map {
                animesQueries.insertNetworkAnime(
                    source = it.source,
                    url = it.url,
                    artist = it.artist,
                    author = it.author,
                    description = it.description,
                    genre = it.genre,
                    title = it.title,
                    status = it.status,
                    thumbnailUrl = it.thumbnailUrl,
                    favorite = it.favorite,
                    lastUpdate = it.lastUpdate,
                    nextUpdate = it.nextUpdate,
                    calculateInterval = it.fetchInterval.toLong(),
                    initialized = it.initialized,
                    viewerFlags = it.viewerFlags,
                    episodeFlags = it.episodeFlags,
                    coverLastModified = it.coverLastModified,
                    dateAdded = it.dateAdded,
                    updateStrategy = it.updateStrategy,
                    version = it.version,
                    updateTitle = it.title.isNotBlank(),
                    updateCover = !it.thumbnailUrl.isNullOrBlank(),
                    updateDetails = it.initialized,
                    mapper = AnimeMapper::mapAnime,
                )
                    .executeAsOne()
            }
        }
    }

    private suspend fun partialUpdate(vararg animeUpdates: AnimeUpdate) {
        handler.await(inTransaction = true) {
            animeUpdates.forEach { value ->
                animesQueries.update(
                    source = value.source,
                    url = value.url,
                    artist = value.artist,
                    author = value.author,
                    description = value.description,
                    genre = value.genre?.let(StringListColumnAdapter::encode),
                    title = value.title,
                    status = value.status,
                    thumbnailUrl = value.thumbnailUrl,
                    favorite = value.favorite,
                    lastUpdate = value.lastUpdate,
                    nextUpdate = value.nextUpdate,
                    calculateInterval = value.fetchInterval?.toLong(),
                    initialized = value.initialized,
                    viewer = value.viewerFlags,
                    episodeFlags = value.episodeFlags,
                    coverLastModified = value.coverLastModified,
                    dateAdded = value.dateAdded,
                    animeId = value.id,
                    updateStrategy = value.updateStrategy?.let(UpdateStrategyColumnAdapter::encode),
                    version = value.version,
                    isSyncing = 0,
                    notes = value.notes,
                )
            }
        }
    }
}
