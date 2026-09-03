package eu.kanade.tachiyomi.data.track.kitsu

import eu.kanade.tachiyomi.data.database.models.Track

fun Track.toApiStatus() = when (status) {
    Kitsu.WATCHING -> "current"
    Kitsu.COMPLETED -> "completed"
    Kitsu.ON_HOLD -> "on_hold"
    Kitsu.DROPPED -> "dropped"
    Kitsu.PLAN_TO_WATCH -> "planned"
    else -> throw Exception("Unknown status")
}
