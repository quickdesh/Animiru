package eu.kanade.tachiyomi.data.coil

import coil3.key.Keyer
import coil3.request.Options
import eu.kanade.domain.anime.model.hasCustomCover
import eu.kanade.tachiyomi.data.cache.CoverCache
import tachiyomi.domain.anime.model.AnimeCover
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import tachiyomi.domain.anime.model.Anime as DomainManga

class MangaKeyer : Keyer<DomainManga> {
    override fun key(data: DomainManga, options: Options): String {
        return if (data.hasCustomCover()) {
            "${data.id};${data.coverLastModified}"
        } else {
            "${data.thumbnailUrl};${data.coverLastModified}"
        }
    }
}

class MangaCoverKeyer(
    private val coverCache: CoverCache = Injekt.get(),
) : Keyer<AnimeCover> {
    override fun key(data: AnimeCover, options: Options): String {
        return if (coverCache.getCustomCoverFile(data.animeId).exists()) {
            "${data.animeId};${data.lastModified}"
        } else {
            "${data.url};${data.lastModified}"
        }
    }
}
