package eu.kanade.tachiyomi.data.backup.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import tachiyomi.domain.items.episode.model.Episode

@Serializable
data class BackupEpisode(
    // in 1.x some of these values have different names
    // url is called key in 1.x
    @ProtoNumber(1) var url: String,
    @ProtoNumber(2) var name: String,
    @ProtoNumber(3) var scanlator: String? = null,
    @ProtoNumber(4) var seen: Boolean = false,
    @ProtoNumber(5) var bookmark: Boolean = false,
    // AM (FILLER) -->
    @ProtoNumber(15) var fillermark: Boolean = false,
    // <-- AM (FILLER)
    // lastPageRead is called progress in 1.x
    @ProtoNumber(6) var lastSecondSeen: Long = 0,
    @ProtoNumber(16) var totalSeconds: Long = 0,
    @ProtoNumber(7) var dateFetch: Long = 0,
    @ProtoNumber(8) var dateUpload: Long = 0,
    // episodeNumber is called number is 1.x
    @ProtoNumber(9) var episodeNumber: Float = 0F,
    @ProtoNumber(10) var sourceOrder: Long = 0,
    @ProtoNumber(11) var lastModifiedAt: Long = 0,
    // AM (SYNC) -->
    @ProtoNumber(12) var version: Long = 0,
    // <-- AM (SYNC)
) {
    fun toEpisodeImpl(): Episode {
        return Episode.create().copy(
            url = this@BackupEpisode.url,
            name = this@BackupEpisode.name,
            episodeNumber = this@BackupEpisode.episodeNumber.toDouble(),
            scanlator = this@BackupEpisode.scanlator,
            seen = this@BackupEpisode.seen,
            bookmark = this@BackupEpisode.bookmark,
            // AM (FILLER) -->
            fillermark = this@BackupEpisode.fillermark,
            // <-- AM (FILLER)
            lastSecondSeen = this@BackupEpisode.lastSecondSeen,
            totalSeconds = this@BackupEpisode.totalSeconds,
            dateFetch = this@BackupEpisode.dateFetch,
            dateUpload = this@BackupEpisode.dateUpload,
            sourceOrder = this@BackupEpisode.sourceOrder,
            lastModifiedAt = this@BackupEpisode.lastModifiedAt,
            // AM (SYNC) -->
            version = this@BackupEpisode.version,
            // <-- AM (SYNC)
        )
    }
}

val backupEpisodeMapper = {
        _: Long,
        _: Long,
        url: String,
        name: String,
        scanlator: String?,
        seen: Boolean,
        bookmark: Boolean,
        // AM (FILLER) -->
        fillermark: Boolean,
        // <-- AM (FILLER)
        lastSecondSeen: Long,
        totalSeconds: Long,
        episodeNumber: Double,
        source_order: Long,
        dateFetch: Long,
        dateUpload: Long,
        lastModifiedAt: Long,
        // AM (SYNC) -->
        version: Long,
        _: Long,
    // <-- AM (SYNC)
    ->
    BackupEpisode(
        url = url,
        name = name,
        episodeNumber = episodeNumber.toFloat(),
        scanlator = scanlator,
        seen = seen,
        bookmark = bookmark,
        // AM (FILLER) -->
        fillermark = fillermark,
        // <-- AM (FILLER)
        lastSecondSeen = lastSecondSeen,
        totalSeconds = totalSeconds,
        dateFetch = dateFetch,
        dateUpload = dateUpload,
        sourceOrder = source_order,
        lastModifiedAt = lastModifiedAt,
        // AM (SYNC) -->
        version = version,
        // <-- AM (SYNC)
    )
}
