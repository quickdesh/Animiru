// AM (SYNC) -->
package eu.kanade.tachiyomi.data.connection.syncmiru.models

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.animiru.AMMR

data class SyncTriggerOptions(
    val syncOnAppStart: Boolean = false,
    val syncOnAppResume: Boolean = false,
    val syncOnEpisodeSeen: Boolean = false,
    val syncOnEpisodeOpen: Boolean = false,
) {
    fun asBooleanArray() = booleanArrayOf(
        syncOnAppStart,
        syncOnAppResume,
        syncOnEpisodeSeen,
        syncOnEpisodeOpen,
    )

    fun anyEnabled() =
        syncOnAppStart ||
            syncOnAppResume ||
            syncOnEpisodeSeen ||
            syncOnEpisodeOpen

    companion object {
        val mainOptions = listOf(
            Entry(
                label = AMMR.strings.sync_on_app_start,
                getter = SyncTriggerOptions::syncOnAppStart,
                setter = { options, enabled -> options.copy(syncOnAppStart = enabled) },
            ),
            Entry(
                label = AMMR.strings.sync_on_app_resume,
                getter = SyncTriggerOptions::syncOnAppResume,
                setter = { options, enabled -> options.copy(syncOnAppResume = enabled) },
            ),
            Entry(
                label = AMMR.strings.sync_on_episode_seen,
                getter = SyncTriggerOptions::syncOnEpisodeSeen,
                setter = { options, enabled -> options.copy(syncOnEpisodeSeen = enabled) },
            ),
            Entry(
                label = AMMR.strings.sync_on_episode_open,
                getter = SyncTriggerOptions::syncOnEpisodeOpen,
                setter = { options, enabled -> options.copy(syncOnEpisodeOpen = enabled) },
            ),
        )

        fun fromBooleanArray(array: BooleanArray) = SyncTriggerOptions(
            syncOnAppStart = array[0],
            syncOnAppResume = array[1],
            syncOnEpisodeSeen = array[2],
            syncOnEpisodeOpen = array[3],
        )
    }

    data class Entry(
        val label: StringResource,
        val getter: (SyncTriggerOptions) -> Boolean,
        val setter: (SyncTriggerOptions, Boolean) -> SyncTriggerOptions,
        val enabled: (SyncTriggerOptions) -> Boolean = { true },
    )
}
// <-- AM (SYNC)
