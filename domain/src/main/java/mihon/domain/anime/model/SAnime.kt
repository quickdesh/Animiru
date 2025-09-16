package mihon.domain.anime.model

import eu.kanade.tachiyomi.animesource.model.SAnime
import tachiyomi.domain.anime.model.Anime

fun SAnime.toDomainAnime(sourceId: Long): Anime {
    return Anime.create().copy(
        url = url,
        // AM (CUSTOM_INFORMATION) -->
        ogTitle = title,
        ogArtist = artist,
        ogAuthor = author,
        ogDescription = description,
        ogGenre = getGenres(),
        ogStatus = status.toLong(),
        // <-- AM (CUSTOM_INFORMATION)
        thumbnailUrl = thumbnail_url,
        // AY -->
        backgroundUrl = background_url,
        // <-- AY
        updateStrategy = update_strategy,
        // AY -->
        fetchType = fetch_type,
        seasonNumber = season_number,
        // <-- AY
        initialized = initialized,
        source = sourceId,
    )
}
