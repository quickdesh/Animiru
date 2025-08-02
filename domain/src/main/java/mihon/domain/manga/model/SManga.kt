package mihon.domain.manga.model

import eu.kanade.tachiyomi.animesource.model.SAnime
import tachiyomi.domain.manga.model.Manga

fun SAnime.toDomainManga(sourceId: Long): Manga {
    return Manga.create().copy(
        url = url,
        title = title,
        artist = artist,
        author = author,
        description = description,
        genre = getGenres(),
        status = status.toLong(),
        thumbnailUrl = thumbnail_url,
        updateStrategy = update_strategy,
        initialized = initialized,
        source = sourceId,
    )
}
