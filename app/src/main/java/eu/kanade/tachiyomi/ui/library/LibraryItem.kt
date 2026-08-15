package eu.kanade.tachiyomi.ui.library

import tachiyomi.domain.library.model.LibraryAnime

data class LibraryItem(
    val libraryAnime: LibraryAnime,
    val downloadCount: Int,
    val unseenCount: Long,
    val isLocal: Boolean,
    val sourceName: String,
    val sourceLanguage: String,
    val badges: Badges,
) {
    val id: Long = libraryAnime.id

    data class Badges(
        val downloadCount: Int,
        val unseenCount: Long,
        val isLocal: Boolean,
        val sourceLanguage: String,
    )
}
