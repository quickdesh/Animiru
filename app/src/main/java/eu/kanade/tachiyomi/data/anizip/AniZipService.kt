package eu.kanade.tachiyomi.data.anizip

import androidx.collection.LruCache
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.data.anizip.model.AniZipEpisodeMeta
import eu.kanade.tachiyomi.data.anizip.model.AniZipResponse
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.json.Json
import logcat.LogPriority
import okhttp3.OkHttpClient
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.system.logcat
import java.util.concurrent.ConcurrentHashMap

@Inject
@SingleIn(AppScope::class)
class AniZipService(
    private val networkHelper: NetworkHelper,
    private val json: Json,
) {
    private val client: OkHttpClient
        get() = networkHelper.client

    private val cache = LruCache<String, Map<String, AniZipEpisodeMeta>>(150)
    private val inFlight = ConcurrentHashMap<String, Deferred<Map<String, AniZipEpisodeMeta>>>()

    suspend fun getMetadata(anilistId: Long? = null, malId: Long? = null): Map<String, AniZipEpisodeMeta> {
        val cacheKey = when {
            anilistId != null && anilistId > 0 -> "anilist_$anilistId"
            malId != null && malId > 0 -> "mal_$malId"
            else -> return emptyMap()
        }

        synchronized(cache) {
            cache.get(cacheKey)?.let { return it }
        }

        val url = when {
            anilistId != null && anilistId > 0 -> "https://api.ani.zip/v1/episodes?anilist_id=$anilistId"
            malId != null && malId > 0 -> "https://api.ani.zip/v1/episodes?mal_id=$malId"
            else -> return emptyMap()
        }

        // Deduplicate in-flight requests per cache key
        val deferred = synchronized(inFlight) {
            inFlight.getOrPut(cacheKey) {
                CoroutineScope(Dispatchers.IO).async {
                    fetchAndCache(url, cacheKey)
                }
            }
        }

        return try {
            deferred.await()
        } finally {
            synchronized(inFlight) {
                inFlight.remove(cacheKey)
            }
        }
    }

    private fun fetchAndCache(url: String, cacheKey: String): Map<String, AniZipEpisodeMeta> {
        return try {
            logcat(LogPriority.INFO) { "AniZip: Fetching metadata from $url" }
            val request = GET(url)
            val response = client.newCall(request).execute()
            response.use { resp ->
                if (!resp.isSuccessful) {
                    logcat(LogPriority.WARN) { "AniZip: Request failed with HTTP ${resp.code} for $url" }
                    // Negative cache to prevent immediate re-querying on 404 / errors
                    synchronized(cache) { cache.put(cacheKey, emptyMap()) }
                    return emptyMap()
                }

                val body = resp.body.string()
                if (body.isBlank()) {
                    synchronized(cache) { cache.put(cacheKey, emptyMap()) }
                    return emptyMap()
                }

                val parsed = json.decodeFromString<AniZipResponse>(body)
                val episodes = parsed.episodes
                if (episodes.isNullOrEmpty()) {
                    synchronized(cache) { cache.put(cacheKey, emptyMap()) }
                    return emptyMap()
                }

                val result = episodes.entries.associate { (key, ep) ->
                    val airDateStr = ep.bestAirDate
                    val airDateMillis = parseAirDateMillis(airDateStr)
                    val episodeNumber = ep.episodeNumber?.toString() ?: key

                    key to AniZipEpisodeMeta(
                        episodeNumber = episodeNumber,
                        title = ep.getPreferredTitle(),
                        overview = ep.bestOverview,
                        image = ep.image?.takeIf { it.isNotBlank() },
                        rating = ep.formattedRating,
                        airDate = airDateStr,
                        airDateMillis = airDateMillis,
                    )
                }

                synchronized(cache) {
                    cache.put(cacheKey, result)
                }

                result
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "AniZip: Error fetching metadata from $url" }
            synchronized(cache) { cache.put(cacheKey, emptyMap()) }
            emptyMap()
        }
    }

    fun findMetaForEpisode(
        metadata: Map<String, AniZipEpisodeMeta>,
        episodeNumber: Double,
        episodeName: String,
    ): AniZipEpisodeMeta? {
        if (metadata.isEmpty()) return null

        // 1. Direct match by recognized episode number (e.g. 1.0 -> "1")
        if (episodeNumber >= 0) {
            val intNum = episodeNumber.toInt()
            if (episodeNumber == intNum.toDouble()) {
                val key = intNum.toString()
                metadata[key]?.let { return it }
                metadata["0$key"]?.let { return it }
            } else {
                metadata[episodeNumber.toString()]?.let { return it }
            }
        }

        // 2. Try parsing leading/contained numbers from episode name
        val regexMatch = Regex("""(?:Episode|Ep\.?|E)\s*(\d+)""", RegexOption.IGNORE_CASE).find(episodeName)
        if (regexMatch != null) {
            val epNum = regexMatch.groupValues[1].toIntOrNull()
            if (epNum != null) {
                metadata[epNum.toString()]?.let { return it }
            }
        }

        // 3. Specials (e.g., "S1", "SP1")
        val specialMatch = Regex("""(?:Special|SP|S)\s*(\d+)""", RegexOption.IGNORE_CASE).find(episodeName)
        if (specialMatch != null) {
            val spNum = specialMatch.groupValues[1]
            metadata["S$spNum"]?.let { return it }
            metadata["SP$spNum"]?.let { return it }
        }

        return null
    }

    companion object {
        fun parseAirDateMillis(dateStr: String?): Long? {
            if (dateStr.isNullOrBlank()) return null
            return try {
                if (dateStr.contains("T")) {
                    java.time.Instant.parse(dateStr).toEpochMilli()
                } else {
                    java.time.LocalDate.parse(dateStr.trim())
                        .atStartOfDay(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
