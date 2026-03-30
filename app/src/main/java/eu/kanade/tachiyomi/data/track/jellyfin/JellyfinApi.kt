// AY -->
package eu.kanade.tachiyomi.data.track.jellyfin

import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.track.jellyfin.dto.JFItem
import eu.kanade.tachiyomi.data.track.jellyfin.dto.JFItemList
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.json.Json
import logcat.LogPriority
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.system.logcat
import uy.kohesive.injekt.injectLazy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.text.split

class JellyfinApi(
    private val trackId: Long,
    private val client: OkHttpClient,
) {
    private val json: Json by injectLazy()

    suspend fun getTrackSearch(url: String): TrackSearch =
        withIOContext {
            try {
                val httpUrl = url.toHttpUrl()
                val fragment = httpUrl.fragment!!

                when {
                    fragment.startsWith("season") -> {
                        getTrackFromSeries(httpUrl)
                    }
                    fragment.startsWith("movie") -> {
                        getTrackFromMovie(httpUrl)
                    }
                    else -> {
                        logcat(LogPriority.WARN) { "Could not recognize item: $url" }
                        throw IllegalArgumentException("Unexpected type: $fragment")
                    }
                }
            } catch (e: Exception) {
                logcat(LogPriority.WARN, e) { "Could not get item: $url" }
                throw e
            }
        }

    private fun getEpisodesUrl(url: HttpUrl): HttpUrl {
        val fragment = url.fragment!!

        return url.newBuilder().apply {
            encodedPath("/")
            fragment(null)
            encodedQuery(null)

            addPathSegment("Shows")
            addPathSegment(fragment.split(",").last())
            addPathSegment("Episodes")
            addQueryParameter("seasonId", url.pathSegments.last())
            addQueryParameter("userId", url.pathSegments[1])
            addQueryParameter("Fields", "Overview,MediaSources")
        }.build()
    }

    private suspend fun getTrackFromMovie(url: HttpUrl): TrackSearch {
        val movie = with(json) {
            client.newCall(GET(url))
                .awaitSuccess()
                .parseAs<JFItem>()
        }

        return TrackSearch.create(trackId).apply {
            this.tracking_url = url.toString()
            this.title = movie.name
            this.total_episodes = 1
            this.last_episode_seen = if (movie.userData.played) 1.0 else 0.0
            this.status = if (movie.userData.played) Jellyfin.COMPLETED else Jellyfin.UNSEEN
        }
    }

    private suspend fun getTrackFromSeries(url: HttpUrl): TrackSearch {
        val seasonId = url.pathSegments.last()
        val seasonUrl = url.newBuilder().apply {
            removePathSegment(3)
            addPathSegment(seasonId)
        }.build()

        val seasonItem = with(json) {
            client.newCall(GET(seasonUrl))
                .awaitSuccess()
                .parseAs<JFItem>()
        }

        val track = TrackSearch.create(trackId).apply {
            this.tracking_url = url.toString()
            this.title = seasonItem.name
        }

        val episodesUrl = getEpisodesUrl(url)
        val episodes = with(json) {
            client.newCall(GET(episodesUrl))
                .awaitSuccess()
                .parseAs<JFItemList>()
        }.items

        if (episodes.isEmpty()) {
            return track.apply {
                this.total_episodes = 0
                this.last_episode_seen = 0.0
                this.status = Jellyfin.UNSEEN
            }
        }

        val totalEpisodes = episodes.last().indexNumber!!
        val firstUnwatched = episodes.indexOfFirst { !it.userData.played }

        if (firstUnwatched == 0) {
            return track.apply {
                this.total_episodes = totalEpisodes
                this.last_episode_seen = 0.0
                this.status = Jellyfin.UNSEEN
            }
        }

        if (firstUnwatched == -1) {
            return track.apply {
                this.total_episodes = totalEpisodes
                this.last_episode_seen = totalEpisodes.toDouble()
                this.status = Jellyfin.COMPLETED
            }
        }

        val lastContinuousSeen = episodes[firstUnwatched - 1].indexNumber!!

        return track.apply {
            this.total_episodes = totalEpisodes
            this.last_episode_seen = lastContinuousSeen.toDouble()
            this.status = Jellyfin.WATCHING
        }
    }

    suspend fun updateProgress(track: Track): Track {
        val httpUrl = track.tracking_url.toHttpUrl()
        val fragment = httpUrl.fragment!!

        val itemId = if (fragment.startsWith("movie")) {
            httpUrl.pathSegments.last().takeIf { track.last_episode_seen > 0.0 }
        } else {
            val episodesUrl = getEpisodesUrl(httpUrl)
            val episodes = with(json) {
                client.newCall(GET(episodesUrl))
                    .awaitSuccess()
                    .parseAs<JFItemList>()
            }.items

            episodes.firstOrNull {
                it.indexNumber!!.equalsTo(track.last_episode_seen)
            }?.id
        }

        if (itemId != null) {
            val time = DATE_FORMATTER.format(Date())
            val postUrl = httpUrl.newBuilder().apply {
                fragment(null)
                removePathSegment(3)
                removePathSegment(2)
                addPathSegment("PlayedItems")
                addPathSegment(itemId)
                addQueryParameter("DatePlayed", time)
            }.build().toString()

            client.newCall(
                POST(postUrl),
            ).awaitSuccess()
        }

        return getTrackSearch(track.tracking_url)
    }

    private fun Long.equalsTo(other: Double): Boolean {
        return abs(this - other) < 0.001
    }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    }
}
// <-- AY
