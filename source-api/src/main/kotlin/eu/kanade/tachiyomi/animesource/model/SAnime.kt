@file:Suppress("PropertyName")

package eu.kanade.tachiyomi.animesource.model

import java.io.Serializable

interface SAnime : Serializable {

    var url: String

    var title: String

    var artist: String?

    var author: String?

    var description: String?

    var genre: String?

    var status: Int

    var thumbnail_url: String?

    // AY -->
    var background_url: String?
    // <-- AY

    var update_strategy: AnimeUpdateStrategy

    var fetch_type: FetchType

    var season_number: Double

    var initialized: Boolean

    fun getGenres(): List<String>? {
        if (genre.isNullOrBlank()) return null
        return genre?.split(", ")?.map { it.trim() }?.filterNot { it.isBlank() }?.distinct()
    }

    // AM (CUSTOM_INFORMATION) -->
    val originalTitle: String
    val originalAuthor: String?
    val originalArtist: String?
    val originalDescription: String?
    val originalGenre: String?
    val originalStatus: Int
    // <-- AM (CUSTOM_INFORMATION)

    fun copy() = create().also {
        it.url = url
        // AM (CUSTOM_INFORMATION) -->
        it.title = originalTitle
        it.artist = originalArtist
        it.author = originalAuthor
        it.description = originalDescription
        it.genre = originalGenre
        it.status = originalStatus
        // <-- AM (CUSTOM_INFORMATION)
        it.thumbnail_url = thumbnail_url
        // AY -->
        it.background_url = background_url
        // <-- AY
        it.update_strategy = update_strategy
        it.fetch_type = fetch_type
        it.season_number = season_number
        it.initialized = initialized
    }

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        const val PUBLISHING_FINISHED = 4
        const val CANCELLED = 5
        const val ON_HIATUS = 6

        fun create(): SAnime {
            return SAnimeImpl()
        }
    }
}

// AM (CUSTOM_INFORMATION) -->
fun SAnime.copy(
    url: String = this.url,
    title: String = this.originalTitle,
    artist: String? = this.originalArtist,
    author: String? = this.originalAuthor,
    description: String? = this.originalDescription,
    genre: String? = this.originalGenre,
    status: Int = this.status,
    thumbnail_url: String? = this.thumbnail_url,
    fetch_type: FetchType = this.fetch_type,
    season_number: Double = this.season_number,
    initialized: Boolean = this.initialized,
) = SAnime.create().also {
    it.url = url
    it.title = title
    it.artist = artist
    it.author = author
    it.description = description
    it.genre = genre
    it.status = status
    it.thumbnail_url = thumbnail_url
    it.fetch_type = fetch_type
    it.season_number = season_number
    it.initialized = initialized
}
// <-- AM (CUSTOM_INFORMATION)
