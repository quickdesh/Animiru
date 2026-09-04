package tachiyomi.domain.anime.model

import tachiyomi.domain.anime.interactor.GetCustomAnimeInfo

// Ugly hack because the alternative would be too much work
object CustomAnimeInfoHolder {
    lateinit var getCustomAnimeInfo: GetCustomAnimeInfo
        private set

    fun init(getCustomAnimeInfo: GetCustomAnimeInfo) {
        this.getCustomAnimeInfo = getCustomAnimeInfo
    }
}
