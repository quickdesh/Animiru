package tachiyomi.domain.library.model

import tachiyomi.domain.anime.model.Anime

data class LibraryAnime(
    val anime: Anime,
    val categories: List<Long>,
    val totalCount: Long,
    val seenCount: Long,
    val bookmarkCount: Long,
    // AM (FILLERMARK) -->
    val fillermarkCount: Long,
    // <-- AM (FILLERMARK)
    val latestUpload: Long,
    val episodeFetchedAt: Long,
    val lastSeen: Long,
) {
    val id: Long = anime.id

    val unseenCount
        get() = totalCount - seenCount

    val hasBookmarks
        get() = bookmarkCount > 0

    // AM (FILLERMARK) -->
    val hasFillermarks
        get() = fillermarkCount > 0
    // <-- AM (FILLERMARK)

    val hasStarted = seenCount > 0
}
