package tachiyomi.domain.storage.service

import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.storage.FolderProvider

class StoragePreferences(
    folderProvider: FolderProvider,
    preferenceStore: PreferenceStore,
) {

    val baseStorageDirectory: Preference<String> = preferenceStore.getString(
        Preference.appStateKey("storage_dir"),
        folderProvider.path(),
    )

    // AM (FILE_SIZE) -->
    val showEpisodeFileSize: Preference<Boolean> = preferenceStore.getBoolean("pref_show_downloaded_episode_size", true)
    // <-- AM (FILE_SIZE)
}
