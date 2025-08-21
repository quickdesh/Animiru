package eu.kanade.tachiyomi.data.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber

object BackupDetector {
    @Serializable
    data class BackupDetector(
        @ProtoNumber(103) val backupSources: List<DetectSource> = emptyList(),
        @ProtoNumber(500) val isLegacy: Boolean = true,
    ) {
        @Serializable
        data class DetectSource(
            @ProtoNumber(1) val name: String = "",
            @ProtoNumber(2) val sourceId: Long,
        )
    }

    /**
     * Try to guess if the backup is an old aniyomi/animiru backup.
     *
     * Returns true if it's (probably) an old aniyomi/animiru backup, or false if it's a
     * new aniyomi/animiru backup.
     */
    fun isLegacyBackup(bytes: ByteArray): Boolean {
        return try {
            val detect = ProtoBuf.decodeFromByteArray(BackupDetector.serializer(), bytes)
            detect.isLegacy && detect.backupSources.isNotEmpty()
        } catch (_: SerializationException) {
            false
        }
    }
}
