package eu.kanade.tachiyomi.data.connection.discord

import eu.kanade.tachiyomi.network.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import logcat.LogPriority
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tachiyomi.core.common.util.system.logcat

class DiscordRPCExternalAsset(
    applicationId: String,
    private val token: String,
    private val client: OkHttpClient,
    private val json: Json,
) {
    @Serializable
    data class ExternalAsset(
        val url: String? = null,
        @SerialName("external_asset_path")
        val externalAssetPath: String? = null,
    )

    private val api = "https://discord.com/api/v9/applications/$applicationId/external-assets"
    suspend fun getDiscordUri(imageUrl: String): String? {
        if (imageUrl.startsWith("mp:")) return imageUrl
        val request = Request.Builder().url(api).header("Authorization", token)
            .post("{\"urls\":[\"$imageUrl\"]}".toRequestBody("application/json".toMediaType()))
            .build()
        return try {
            val res = client.newCall(request).await()
            if (res.code == 429) {
                // Rate limit hit
                res.close()
                return null
            }
            if (!res.isSuccessful) {
                logcat(LogPriority.ERROR) { "Discord API error: HTTP ${res.code} - ${res.body.string()}" }
                res.close()
                return null
            }
            json.decodeFromString<List<ExternalAsset>>(res.body.string())
                .firstOrNull()?.externalAssetPath?.let { "mp:$it" }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Exception while fetching Discord external asset" }
            null
        }
    }
}
