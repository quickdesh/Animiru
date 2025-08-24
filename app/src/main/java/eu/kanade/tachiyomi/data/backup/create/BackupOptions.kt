package eu.kanade.tachiyomi.data.backup.create

import dev.icerock.moko.resources.StringResource
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.i18n.MR
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.i18n.aniyomi.AYMR

data class BackupOptions(
    val libraryEntries: Boolean = true,
    val categories: Boolean = true,
    val episodes: Boolean = true,
    val tracking: Boolean = true,
    val history: Boolean = true,
    val seenEntries: Boolean = true,
    val appSettings: Boolean = true,
    val extensionRepoSettings: Boolean = true,
    // AY -->
    val customButton: Boolean = true,
    // <-- AY
    val sourceSettings: Boolean = true,
    val privateSettings: Boolean = false,
    // AY -->
    val extensions: Boolean = false,
    // <-- AY
    // AM (CUSTOM_INFORMATION) -->
    val customInfo: Boolean = false,
    // <-- AM (CUSTOM_INFORMATION)
) {

    fun asBooleanArray() = booleanArrayOf(
        libraryEntries,
        categories,
        episodes,
        tracking,
        history,
        seenEntries,
        appSettings,
        extensionRepoSettings,
        // AY -->
        customButton,
        // <-- AY
        sourceSettings,
        privateSettings,
        // AY -->
        extensions,
        // <-- AY
        // AM (CUSTOM_INFORMATION) -->
        customInfo,
        // <-- AM (CUSTOM_INFORMATION)
    )

    fun canCreate() = libraryEntries ||
        categories ||
        appSettings ||
        extensionRepoSettings ||
        // AY -->
        customButton ||
        // <-- AY
        sourceSettings

    companion object {
        val libraryOptions = persistentListOf(
            Entry(
                label = MR.strings.manga,
                getter = BackupOptions::libraryEntries,
                setter = { options, enabled -> options.copy(libraryEntries = enabled) },
            ),
            Entry(
                label = AYMR.strings.episodes,
                getter = BackupOptions::episodes,
                setter = { options, enabled -> options.copy(episodes = enabled) },
                enabled = { it.libraryEntries },
            ),
            Entry(
                label = MR.strings.track,
                getter = BackupOptions::tracking,
                setter = { options, enabled -> options.copy(tracking = enabled) },
                enabled = { it.libraryEntries },
            ),
            Entry(
                label = MR.strings.history,
                getter = BackupOptions::history,
                setter = { options, enabled -> options.copy(history = enabled) },
                enabled = { it.libraryEntries },
            ),
            Entry(
                label = MR.strings.categories,
                getter = BackupOptions::categories,
                setter = { options, enabled -> options.copy(categories = enabled) },
            ),
            Entry(
                label = AMMR.strings.am_non_library_settings,
                getter = BackupOptions::seenEntries,
                setter = { options, enabled -> options.copy(seenEntries = enabled) },
                enabled = { it.libraryEntries },
            ),
            // AM (CUSTOM_INFORMATION) -->
            Entry(
                label = AMMR.strings.custom_entry_info,
                getter = BackupOptions::customInfo,
                setter = { options, enabled -> options.copy(customInfo = enabled) },
                enabled = { it.libraryEntries },
            ),
            // <-- AM (CUSTOM_INFORMATION)
        )

        val settingsOptions = persistentListOf(
            Entry(
                label = MR.strings.app_settings,
                getter = BackupOptions::appSettings,
                setter = { options, enabled -> options.copy(appSettings = enabled) },
            ),
            Entry(
                label = MR.strings.extensionRepo_settings,
                getter = BackupOptions::extensionRepoSettings,
                setter = { options, enabled -> options.copy(extensionRepoSettings = enabled) },
            ),
            // AY -->
            Entry(
                label = AYMR.strings.custom_button_settings,
                getter = BackupOptions::customButton,
                setter = { options, enabled -> options.copy(customButton = enabled) },
            ),
            // <-- AY
            Entry(
                label = MR.strings.source_settings,
                getter = BackupOptions::sourceSettings,
                setter = { options, enabled -> options.copy(sourceSettings = enabled) },
            ),
            Entry(
                label = MR.strings.private_settings,
                getter = BackupOptions::privateSettings,
                setter = { options, enabled -> options.copy(privateSettings = enabled) },
                enabled = { it.appSettings || it.sourceSettings },
            ),
        )

        // AY -->
        val extensionOptions = persistentListOf(
            Entry(
                label = MR.strings.label_extensions,
                getter = BackupOptions::extensions,
                setter = { options, enabled -> options.copy(extensions = enabled) },
            ),
        )
        // <-- AY

        fun fromBooleanArray(array: BooleanArray) = BackupOptions(
            libraryEntries = array[0],
            categories = array[1],
            episodes = array[2],
            tracking = array[3],
            history = array[4],
            seenEntries = array[5],
            appSettings = array[6],
            extensionRepoSettings = array[7],
            // AY -->
            customButton = array[8],
            // <-- AY
            sourceSettings = array[9],
            privateSettings = array[10],
            // AY -->
            extensions = array[11],
            // <-- AY
            // AM (CUSTOM_INFORMATION) -->
            customInfo = array[12],
            // <-- AM (CUSTOM_INFORMATION)
        )
    }

    data class Entry(
        val label: StringResource,
        val getter: (BackupOptions) -> Boolean,
        val setter: (BackupOptions, Boolean) -> BackupOptions,
        val enabled: (BackupOptions) -> Boolean = { true },
    )
}
