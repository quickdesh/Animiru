package aniyomi.domain.anime

import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.library.model.LibraryAnime

data class SeasonAnime(
    val anime: Anime,
    val totalCount: Long,
    val seenCount: Long,
    val bookmarkCount: Long,
    val latestUpload: Long,
    val fetchedAt: Long,
    val lastSeen: Long,
) {
    val id: Long = anime.id

    val seen
        get() = totalCount == seenCount

    val unseenCount
        get() = totalCount - seenCount

    val hasBookmarks
        get() = bookmarkCount > 0

    val hasStarted = seenCount > 0

    fun toLibraryAnime(): LibraryAnime {
        return LibraryAnime(
            anime = anime,
            categories = emptyList(),
            totalCount = totalCount,
            seenCount = seenCount,
            bookmarkCount = bookmarkCount,
            fillermarkCount = 0L, // TODO(fillermark)
            latestUpload = latestUpload,
            episodeFetchedAt = fetchedAt,
            lastSeen = lastSeen,
        )
    }
}
