package animiru.domain.torrent.service

import aniyomi.core.common.torrent.DisabledTorrServerException
import aniyomi.core.common.torrent.TorrentHelpers
import aniyomi.core.common.torrent.TorrentServerApi
import aniyomi.core.common.torrent.model.Torrent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.get
import eu.kanade.tachiyomi.torrentutils.model.DeadTorrentException
import eu.kanade.tachiyomi.torrentutils.model.TorrentFile
import eu.kanade.tachiyomi.torrentutils.model.TorrentInfo
import java.net.SocketTimeoutException

@Inject
@SingleIn(AppScope::class)
class TorrentUtilsImpl(
    private val torrentServerApi: TorrentServerApi,
    private val network: NetworkHelper,
) {

    suspend fun getTorrentInfo(
        url: String,
        title: String,
    ): TorrentInfo {
        val torrent: Torrent = if (url.startsWith("magnet")) {
            // Magnet links need to be added to the torrent server to retrieve their
            // information
            try {
                torrentServerApi.addTorrent(url, title, "", "", false)
            } catch (_: SocketTimeoutException) {
                throw DeadTorrentException()
            } catch (_: Exception) {
                throw DisabledTorrServerException()
            }
        } else {
            // For torrent files we can parse the information out of the file itself
            // without starting the torrent server
            network.client.get(url).use { response ->
                TorrentHelpers.parseTorrentDetailsFromTorrentFileContent(response.body.byteStream())
            }
        }
        return torrentToTorrentInfo(torrent, title)
    }

    private fun torrentToTorrentInfo(torrent: Torrent, overrideTitle: String?): TorrentInfo {
        return TorrentInfo(
            overrideTitle ?: torrent.title,
            torrent.fileStats?.map { file ->
                TorrentFile(file.path, file.id ?: 0, file.length, torrent.hash!!, torrent.trackers ?: emptyList())
            } ?: emptyList(),
            torrent.hash!!,
            torrent.torrentSize ?: -1L,
            torrent.trackers ?: emptyList(),
        )
    }
}
