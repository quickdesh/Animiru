package eu.kanade.tachiyomi.util

import eu.kanade.domain.anime.interactor.UpdateAnime
import eu.kanade.domain.anime.model.hasCustomBackground
import eu.kanade.domain.anime.model.hasCustomCover
import eu.kanade.domain.anime.model.toSAnime
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.data.cache.BackgroundCache
import eu.kanade.tachiyomi.data.cache.CoverCache
import tachiyomi.domain.anime.model.Anime
import tachiyomi.source.local.image.LocalBackgroundManager
import tachiyomi.source.local.image.LocalCoverManager
import tachiyomi.source.local.image.LocalEpisodeThumbnailManager
import tachiyomi.source.local.isLocal
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.InputStream
import java.time.Instant

/**
 * Call before updating [Anime.thumbnail_url] to ensure old cover can be cleared from cache
 */
fun Anime.prepUpdateCover(coverCache: CoverCache, remoteAnime: SAnime, refreshSameUrl: Boolean): Anime {
    // Never refresh covers if the new url is null, as the current url has possibly become invalid
    val newUrl = remoteAnime.thumbnail_url ?: return this

    // Never refresh covers if the url is empty to avoid "losing" existing covers
    if (newUrl.isEmpty()) return this

    if (!refreshSameUrl && thumbnailUrl == newUrl) return this

    return when {
        isLocal() -> {
            this.copy(coverLastModified = Instant.now().toEpochMilli())
        }
        hasCustomCover(coverCache) -> {
            coverCache.deleteFromCache(this, false)
            this
        }
        else -> {
            coverCache.deleteFromCache(this, false)
            this.copy(coverLastModified = Instant.now().toEpochMilli())
        }
    }
}

// AY -->

/**
 * Call before updating [Anime.background_url] to ensure old background can be cleared from cache
 */
fun Anime.prepUpdateBackground(
    backgroundCache: BackgroundCache,
    remoteAnime: SAnime,
    refreshSameUrl: Boolean,
): Anime {
    // Never refresh backgrounds if the new url is null, as the current url has possibly become invalid
    val newUrl = remoteAnime.background_url ?: return this

    // Never refresh covers if the url is empty to avoid "losing" existing backgrounds
    if (newUrl.isEmpty()) return this

    if (!refreshSameUrl && backgroundUrl == newUrl) return this

    return when {
        isLocal() -> {
            this.copy(backgroundLastModified = Instant.now().toEpochMilli())
        }
        hasCustomBackground(backgroundCache) -> {
            backgroundCache.deleteFromCache(this, false)
            this
        }
        else -> {
            backgroundCache.deleteFromCache(this, false)
            this.copy(backgroundLastModified = Instant.now().toEpochMilli())
        }
    }
}
// <-- AY

fun Anime.removeCovers(coverCache: CoverCache = Injekt.get()): Anime {
    if (isLocal()) return this
    return if (coverCache.deleteFromCache(this, true) > 0) {
        return copy(coverLastModified = Instant.now().toEpochMilli())
    } else {
        this
    }
}

// AY -->
fun Anime.removeBackgrounds(backgroundCache: BackgroundCache): Anime {
    if (isLocal()) return this
    return if (backgroundCache.deleteFromCache(this, true) > 0) {
        return copy(backgroundLastModified = Instant.now().toEpochMilli())
    } else {
        this
    }
}
// <-- AY

suspend fun Anime.editCover(
    coverManager: LocalCoverManager,
    stream: InputStream,
    updateAnime: UpdateAnime = Injekt.get(),
    coverCache: CoverCache = Injekt.get(),
) {
    if (isLocal()) {
        coverManager.update(toSAnime(), stream)
        updateAnime.awaitUpdateCoverLastModified(id)
    } else if (favorite) {
        coverCache.setCustomCoverToCache(this, stream)
        updateAnime.awaitUpdateCoverLastModified(id)
    }
}

// AY -->
suspend fun Anime.editBackground(
    backgroundManager: LocalBackgroundManager,
    stream: InputStream,
    updateAnime: UpdateAnime = Injekt.get(),
    backgroundCache: BackgroundCache = Injekt.get(),
) {
    if (isLocal()) {
        backgroundManager.update(toSAnime(), stream)
        updateAnime.awaitUpdateBackgroundLastModified(id)
    } else if (favorite) {
        backgroundCache.setCustomBackgroundToCache(this, stream)
        updateAnime.awaitUpdateBackgroundLastModified(id)
    }
}

fun SEpisode.editThumbnail(
    anime: Anime,
    thumbnailManager: LocalEpisodeThumbnailManager,
    stream: InputStream,
) {
    if (anime.isLocal()) {
        thumbnailManager.update(anime.toSAnime(), this, stream)
    }
}
// <-- AY
