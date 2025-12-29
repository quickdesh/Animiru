package eu.kanade.tachiyomi.torrentutils.model

data class TorrentFile(
    val path: String,
    val indexFile: Int,
    val size: Long,
    private val torrentHash: String,
    private val trackers: List<String> = emptyList(),
) {
    fun toMagnetURI(): String {
        throw Exception("Please KYS! This is a stub!")
    }
}
