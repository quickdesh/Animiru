package eu.kanade.tachiyomi.data.track.hikka

import android.net.Uri
import androidx.core.net.toUri
import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.track.hikka.dto.HKAnime
import eu.kanade.tachiyomi.data.track.hikka.dto.HKAnimePagination
import eu.kanade.tachiyomi.data.track.hikka.dto.HKOAuth
import eu.kanade.tachiyomi.data.track.hikka.dto.HKUser
import eu.kanade.tachiyomi.data.track.hikka.dto.HKWatch
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.network.DELETE
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.HttpException
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.PUT
import eu.kanade.tachiyomi.network.await
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.jsonMime
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.domain.track.model.Track as DomainTrack

class HikkaApi(
    private val trackId: Long,
    private val client: OkHttpClient,
    private val json: Json,
    interceptor: HikkaInterceptor,
) {
    suspend fun getCurrentUser(): HKUser {
        return withIOContext {
            val request = Request.Builder()
                .url("${BASE_API_URL}/user/me")
                .get()
                .build()
            with(json) {
                authClient.newCall(request)
                    .awaitSuccess()
                    .parseAs<HKUser>()
            }
        }
    }

    suspend fun accessToken(reference: String): HKOAuth {
        return withIOContext {
            with(json) {
                client.newCall(authTokenCreate(reference))
                    .awaitSuccess()
                    .parseAs<HKOAuth>()
            }
        }
    }

    suspend fun searchAnime(query: String): List<TrackSearch> {
        return withIOContext {
            val url = "$BASE_API_URL/anime".toUri().buildUpon()
                .appendQueryParameter("page", "1")
                .appendQueryParameter("size", "50")
                .build()

            val payload = buildJsonObject {
                put("genres", buildJsonArray { })
                put("media_type", buildJsonArray { })
                put("only_translated", false)
                put("query", query)
                put("rating", buildJsonArray { })
                put("season", buildJsonArray { })
                put(
                    "sort",
                    buildJsonArray {
                        add("score:desc")
                        add("scored_by:desc")
                    },
                )
                put("status", buildJsonArray { })
                put("studios", buildJsonArray { })
                put("years", buildJsonArray { })
            }

            with(json) {
                authClient.newCall(POST(url.toString(), body = payload.toString().toRequestBody(jsonMime)))
                    .awaitSuccess()
                    .parseAs<HKAnimePagination>()
                    .list
                    .map { it.toTrack(trackId) }
            }
        }
    }

    suspend fun getSeen(track: Track): HKWatch? {
        return withIOContext {
            val slug = track.tracking_url.split("/")[4]
            val url = "$BASE_API_URL/watch/$slug".toUri().buildUpon().build()
            with(json) {
                try {
                    authClient.newCall(GET(url.toString()))
                        .awaitSuccess()
                        .parseAs<HKWatch>()
                } catch (e: HttpException) {
                    if (e.code == 404) {
                        null
                    } else {
                        throw e
                    }
                }
            }
        }
    }

    suspend fun getAnimeDetails(slug: String): TrackSearch? {
        return withIOContext {
            val url = "$BASE_API_URL/anime/$slug"

            with(json) {
                val response = authClient.newCall(GET(url))
                    .await()

                if (response.code == 404) {
                    null
                } else {
                    response
                        .parseAs<HKAnime>()
                        .toTrack(trackId)
                }
            }
        }
    }

    suspend fun getAnime(track: Track): TrackSearch {
        return withIOContext {
            val slug = track.tracking_url.split("/")[4]
            val url = "$BASE_API_URL/anime/$slug".toUri().buildUpon()
                .build()

            with(json) {
                authClient.newCall(GET(url.toString()))
                    .awaitSuccess()
                    .parseAs<HKAnime>()
                    .toTrack(trackId)
            }
        }
    }

    suspend fun deleteUserAnime(track: DomainTrack) {
        return withIOContext {
            val slug = track.remoteUrl.split("/")[4]

            val url = "$BASE_API_URL/watch/$slug".toUri().buildUpon()
                .build()

            authClient.newCall(DELETE(url.toString()))
                .awaitSuccess()
        }
    }

    suspend fun addUserAnime(track: Track): Track {
        return withIOContext {
            val slug = track.tracking_url.split("/")[4]

            val url = "$BASE_API_URL/watch/$slug".toUri().buildUpon()
                .build()

            var rewatches = getSeen(track)?.rewatches ?: 0
            if (track.status == Hikka.REWATCHING && rewatches == 0) {
                rewatches = 1
            }

            val payload = buildJsonObject {
                put("note", "")
                put("episodes", track.last_episode_seen.toInt())
                put("rewatches", rewatches)
                put("score", track.score.toInt())
                put("status", track.toApiStatus())
                put("start_date", if (track.started_watching_date > 0L) track.started_watching_date / 1000 else null)
                put("end_date", if (track.finished_watching_date > 0L) track.finished_watching_date / 1000 else null)
            }

            with(json) {
                authClient.newCall(PUT(url.toString(), body = payload.toString().toRequestBody(jsonMime)))
                    .awaitSuccess()
                    .parseAs<HKWatch>()
                    .toTrack(trackId)
            }
        }
    }

    suspend fun updateUserAnime(track: Track): Track = addUserAnime(track)

    private val authClient = client.newBuilder().addInterceptor(interceptor).build()

    companion object {
        const val BASE_API_URL = "https://api.hikka.io"
        const val BASE_URL = "https://hikka.io"
        private const val SCOPE = "watchlist,read:user-details"
        private const val CLIENT_REFERENCE = "046d1b3e-6415-4b92-8484-ed3ec68441c0"
        private const val CLIENT_SECRET = "FS5mtHuqlOHg0OqyTXC_6EsNac9XIMT0LCFzlmPRWFQo3lgjfFjDXQ" +
            "D5Lm9n_IqH8QL7ywbuEAAtJ3_pEvSZiwpROrt3TRj5_JdBgNdhAxul" +
            "QZgfiAnRzj21FzOd03yx"

        fun authUrl(): Uri = "$BASE_URL/oauth".toUri().buildUpon()
            .appendQueryParameter("reference", CLIENT_REFERENCE)
            .appendQueryParameter("scope", SCOPE)
            .build()

        fun refreshTokenRequest(accessToken: String): Request {
            val headers = Headers.Builder()
                .add("auth", accessToken)
                .build()

            return GET("$BASE_API_URL/user/me", headers = headers) // Any request with auth
        }

        fun authTokenCreate(reference: String): Request {
            val payload = buildJsonObject {
                put("request_reference", reference)
                put("client_secret", CLIENT_SECRET)
            }
            return POST("$BASE_API_URL/auth/token", body = payload.toString().toRequestBody(jsonMime))
        }

        fun authTokenInfo(accessToken: String): Request {
            val headers = Headers.Builder()
                .add("auth", accessToken)
                .build()

            return GET("$BASE_API_URL/auth/token/info", headers = headers)
        }
    }
}
