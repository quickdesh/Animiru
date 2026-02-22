package tachiyomi.domain.download.service

import tachiyomi.core.common.preference.PreferenceStore

class DownloadPreferences(
    private val preferenceStore: PreferenceStore,
) {

    fun downloadOnlyOverWifi() = preferenceStore.getBoolean(
        "pref_download_only_over_wifi_key",
        true,
    )

    // AY -->
    fun useExternalDownloader() = preferenceStore.getBoolean("use_external_downloader", false)

    fun externalDownloaderSelection() = preferenceStore.getString(
        "external_downloader_selection",
        "",
    )
    // <-- AY

    fun autoDownloadWhileWatching() = preferenceStore.getInt("auto_download_while_watching", 0)

    fun removeAfterSeenSlots() = preferenceStore.getInt("remove_after_seen_slots", -1)

    fun removeAfterMarkedAsSeen() = preferenceStore.getBoolean(
        "pref_remove_after_marked_as_seen_key",
        false,
    )

    fun removeBookmarkedEpisodes() = preferenceStore.getBoolean("pref_remove_bookmarked", false)

    // AY -->
    fun downloadFillermarkedEpisodes() = preferenceStore.getBoolean("pref_no_download_fillermarked", false)
    // <-- AY

    fun removeExcludeCategories() = preferenceStore.getStringSet(REMOVE_EXCLUDE_CATEGORIES_PREF_KEY, emptySet())

    fun downloadNewEpisodes() = preferenceStore.getBoolean("download_new_episode", false)

    fun downloadNewEpisodeCategories() = preferenceStore.getStringSet(DOWNLOAD_NEW_CATEGORIES_PREF_KEY, emptySet())

    fun downloadNewEpisodeCategoriesExclude() =
        preferenceStore.getStringSet(DOWNLOAD_NEW_CATEGORIES_EXCLUDE_PREF_KEY, emptySet())

    fun downloadNewUnseenEpisodesOnly() = preferenceStore.getBoolean("download_new_unseen_episodes_only", false)

    fun parallelSourceLimit() = preferenceStore.getInt("download_parallel_source_limit", 5)

    companion object {
        private const val REMOVE_EXCLUDE_CATEGORIES_PREF_KEY = "remove_exclude_categories"
        private const val DOWNLOAD_NEW_CATEGORIES_PREF_KEY = "download_new_categories"
        private const val DOWNLOAD_NEW_CATEGORIES_EXCLUDE_PREF_KEY = "download_new_categories_exclude"
        val categoryPreferenceKeys = setOf(
            REMOVE_EXCLUDE_CATEGORIES_PREF_KEY,
            DOWNLOAD_NEW_CATEGORIES_PREF_KEY,
            DOWNLOAD_NEW_CATEGORIES_EXCLUDE_PREF_KEY,
        )
    }
}
