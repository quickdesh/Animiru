// AM (CUSTOM_INFORMATION) -->
package tachiyomi.domain.anime.repository

import tachiyomi.domain.anime.model.CustomAnimeInfo

interface CustomAnimeRepository {

    fun get(animeId: Long): CustomAnimeInfo?

    fun set(animeInfo: CustomAnimeInfo)
}
// <-- AM (CUSTOM_INFORMATION)
