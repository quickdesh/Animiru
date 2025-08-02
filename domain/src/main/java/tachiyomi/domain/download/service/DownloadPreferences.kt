package tachiyomi.domain.download.service

import tachiyomi.core.common.preference.PreferenceStore

// TODO(mihon): Migrate keys
class DownloadPreferences(
    private val preferenceStore: PreferenceStore,
) {

    fun downloadOnlyOverWifi() = preferenceStore.getBoolean(
        "pref_download_only_over_wifi_key",
        true,
    )

    fun useExternalDownloader() = preferenceStore.getBoolean("use_external_downloader", false)

    fun externalDownloaderSelection() = preferenceStore.getString(
        "external_downloader_selection",
        "",
    )

    fun autoDownloadWhileWatching() = preferenceStore.getInt("auto_download_while_watching", 0)

    // TODO(mihon): Add migration `remove_after_read_slots` -> `remove_after_seen_slots`
    fun removeAfterSeenSlots() = preferenceStore.getInt("remove_after_seen_slots", -1)

    // TODO(mihon): Add migration `pref_remove_after_marked_as_read_key` -> `pref_remove_after_marked_as_seen_key`
    fun removeAfterMarkedAsSeen() = preferenceStore.getBoolean(
        "pref_remove_after_marked_as_seen_key",
        false,
    )

    fun removeBookmarkedEpisodes() = preferenceStore.getBoolean("pref_remove_bookmarked", false)

    fun removeExcludeCategories() = preferenceStore.getStringSet(REMOVE_EXCLUDE_CATEGORIES_PREF_KEY, emptySet())

    fun downloadNewEpisodes() = preferenceStore.getBoolean("download_new_episode", false)

    fun downloadNewEpisodeCategories() = preferenceStore.getStringSet(DOWNLOAD_NEW_CATEGORIES_PREF_KEY, emptySet())

    fun downloadNewEpisodeCategoriesExclude() =
        preferenceStore.getStringSet(DOWNLOAD_NEW_CATEGORIES_EXCLUDE_PREF_KEY, emptySet())

    // TODO(mihon): Add migration `download_new_unread_episodes_only` -> `download_new_unseen_episodes_only`
    fun downloadNewUnseenEpisodesOnly() = preferenceStore.getBoolean("download_new_unseen_episodes_only", false)

    companion object {
        private const val REMOVE_EXCLUDE_CATEGORIES_PREF_KEY = "remove_exclude_anime_categories"
        private const val DOWNLOAD_NEW_CATEGORIES_PREF_KEY = "download_new_anime_categories"
        private const val DOWNLOAD_NEW_CATEGORIES_EXCLUDE_PREF_KEY = "download_new_anime_categories_exclude"
        val categoryPreferenceKeys = setOf(
            REMOVE_EXCLUDE_CATEGORIES_PREF_KEY,
            DOWNLOAD_NEW_CATEGORIES_PREF_KEY,
            DOWNLOAD_NEW_CATEGORIES_EXCLUDE_PREF_KEY,
        )
    }
}
