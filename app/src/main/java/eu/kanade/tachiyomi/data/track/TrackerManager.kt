package eu.kanade.tachiyomi.data.track

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.data.track.anilist.Anilist
import eu.kanade.tachiyomi.data.track.bangumi.Bangumi
import eu.kanade.tachiyomi.data.track.hikka.Hikka
import eu.kanade.tachiyomi.data.track.jellyfin.Jellyfin
import eu.kanade.tachiyomi.data.track.kitsu.Kitsu
import eu.kanade.tachiyomi.data.track.myanimelist.MyAnimeList
import eu.kanade.tachiyomi.data.track.shikimori.Shikimori
import eu.kanade.tachiyomi.data.track.simkl.Simkl
import kotlinx.coroutines.flow.combine

@Inject
@SingleIn(AppScope::class)
class TrackerManager {

    companion object {
        const val ANILIST = 2L
        const val KITSU = 3L

        // AY -->
        const val SIMKL = 101L
        const val JELLYFIN = 102L
        // <-- AY
    }

    val myAnimeList = MyAnimeList(1L)
    val aniList = Anilist(ANILIST)
    val kitsu = Kitsu(KITSU)
    val shikimori = Shikimori(4L)
    val bangumi = Bangumi(5L)

    // AY -->
    val simkl = Simkl(SIMKL)
    val jellyfin = Jellyfin(JELLYFIN)
    // <-- AY

    val hikka = Hikka(6L)

    val trackers = listOf(
        myAnimeList,
        aniList,
        kitsu,
        shikimori,
        bangumi,
        // AY -->
        simkl,
        jellyfin,
        // <-- AY
        hikka,
    )

    fun loggedInTrackers() = trackers.filter { it.isLoggedIn }

    fun loggedInTrackersFlow() = combine(trackers.map { it.isLoggedInFlow }) {
        it.mapIndexedNotNull { index, isLoggedIn ->
            if (isLoggedIn) trackers[index] else null
        }
    }

    fun get(id: Long) = trackers.find { it.id == id }

    fun getAll(ids: Set<Long>) = trackers.filter { it.id in ids }
}
