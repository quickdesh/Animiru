package eu.kanade.tachiyomi.util

import eu.kanade.domain.anime.interactor.UpdateAnime
import eu.kanade.domain.anime.model.toSAnime
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

fun Anime.removeCovers(coverCache: CoverCache = Injekt.get()): Anime {
    if (isLocal()) return this
    return if (coverCache.deleteFromCache(this, true) > 0) {
        copy(coverLastModified = Instant.now().toEpochMilli())
    } else {
        this
    }
}

// AY -->
fun Anime.removeBackgrounds(backgroundCache: BackgroundCache): Anime {
    if (isLocal()) return this
    return if (backgroundCache.deleteFromCache(this, true) > 0) {
        copy(backgroundLastModified = Instant.now().toEpochMilli())
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
