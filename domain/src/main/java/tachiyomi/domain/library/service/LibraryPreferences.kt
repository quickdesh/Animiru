package tachiyomi.domain.library.service

import aniyomi.domain.anime.SeasonDisplayMode
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.preference.TriState
import tachiyomi.core.common.preference.getEnum
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.library.model.GroupLibraryMode
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.model.LibraryGroup
import tachiyomi.domain.library.model.LibrarySort

class LibraryPreferences(
    private val preferenceStore: PreferenceStore,
) {

    fun displayMode() = preferenceStore.getObjectFromString(
        "pref_display_mode_library",
        LibraryDisplayMode.default,
        LibraryDisplayMode.Serializer::serialize,
        LibraryDisplayMode.Serializer::deserialize,
    )

    fun sortingMode() = preferenceStore.getObjectFromString(
        "library_sorting_mode",
        LibrarySort.default,
        LibrarySort.Serializer::serialize,
        LibrarySort.Serializer::deserialize,
    )

    fun randomSortSeed() = preferenceStore.getInt("library_random_sort_seed", 0)

    fun portraitColumns() = preferenceStore.getInt("pref_library_columns_portrait_key", 0)

    fun landscapeColumns() = preferenceStore.getInt("pref_library_columns_landscape_key", 0)

    fun lastUpdatedTimestamp() = preferenceStore.getLong(Preference.appStateKey("library_update_last_timestamp"), 0L)
    fun autoUpdateInterval() = preferenceStore.getInt("pref_library_update_interval_key", 0)

    fun autoUpdateDeviceRestrictions() = preferenceStore.getStringSet(
        "library_update_restriction",
        setOf(
            DEVICE_ONLY_ON_WIFI,
        ),
    )
    fun autoUpdateAnimeRestrictions() = preferenceStore.getStringSet(
        "library_update_anime_restriction",
        setOf(
            ANIME_HAS_UNSEEN,
            ANIME_NON_COMPLETED,
            ANIME_NON_SEEN,
            ANIME_OUTSIDE_RELEASE_PERIOD,
        ),
    )

    fun autoUpdateMetadata() = preferenceStore.getBoolean("auto_update_metadata", false)

    fun showContinueWatchingButton() = preferenceStore.getBoolean(
        "display_continue_watching_button",
        false,
    )

    fun markDuplicateSeenEpisodeAsSeen() = preferenceStore.getStringSet("mark_duplicate_seen_episode_seen", emptySet())

    // region Filter

    fun filterDownloaded() = preferenceStore.getEnum(
        "pref_filter_library_downloaded_v2",
        TriState.DISABLED,
    )

    fun filterUnseen() = preferenceStore.getEnum("pref_filter_library_unseen_v2", TriState.DISABLED)

    fun filterStarted() = preferenceStore.getEnum(
        "pref_filter_library_started_v2",
        TriState.DISABLED,
    )

    fun filterBookmarked() = preferenceStore.getEnum(
        "pref_filter_library_bookmarked_v2",
        TriState.DISABLED,
    )

    // AM (FILLERMARK) -->
    fun filterFillermarked() =
        preferenceStore.getEnum("pref_filter_library_fillermarked_v2", TriState.DISABLED)
    // <-- AM (FILLERMARK)

    fun filterCompleted() = preferenceStore.getEnum(
        "pref_filter_library_completed_v2",
        TriState.DISABLED,
    )

    fun filterIntervalCustom() = preferenceStore.getEnum(
        "pref_filter_library_interval_custom",
        TriState.DISABLED,
    )

    fun filterTracking(id: Int) = preferenceStore.getEnum(
        "pref_filter_library_tracked_${id}_v2",
        TriState.DISABLED,
    )

    // endregion

    // region Badges

    fun downloadBadge() = preferenceStore.getBoolean("display_download_badge", false)

    fun unseenBadge() = preferenceStore.getBoolean("display_unseen_badge", true)

    fun localBadge() = preferenceStore.getBoolean("display_local_badge", true)

    fun languageBadge() = preferenceStore.getBoolean("display_language_badge", false)

    fun newShowUpdatesCount() = preferenceStore.getBoolean("library_show_updates_count", true)
    fun newUpdatesCount() = preferenceStore.getInt(Preference.appStateKey("library_unseen_updates_count"), 0)

    // endregion

    // region Category

    fun defaultCategory() = preferenceStore.getInt(DEFAULT_CATEGORY_PREF_KEY, -1)

    fun lastUsedCategory() = preferenceStore.getInt(Preference.appStateKey("last_used_category"), 0)

    fun categoryTabs() = preferenceStore.getBoolean("display_category_tabs", true)

    fun categoryNumberOfItems() = preferenceStore.getBoolean("display_number_of_items", false)

    fun categorizedDisplaySettings() = preferenceStore.getBoolean("categorized_display", false)

    // AY -->
    fun hideHiddenCategoriesSettings() = preferenceStore.getBoolean("hidden_categories", false)
    // <-- AY

    fun updateCategories() = preferenceStore.getStringSet(LIBRARY_UPDATE_CATEGORIES_PREF_KEY, emptySet())

    fun updateCategoriesExclude() = preferenceStore.getStringSet(LIBRARY_UPDATE_CATEGORIES_EXCLUDE_PREF_KEY, emptySet())

    // endregion

    // region Episode

    fun filterEpisodeBySeen() = preferenceStore.getLong(
        "default_episode_filter_by_seen",
        Anime.SHOW_ALL,
    )

    fun filterEpisodeByDownloaded() = preferenceStore.getLong(
        "default_episode_filter_by_downloaded",
        Anime.SHOW_ALL,
    )

    fun filterEpisodeByBookmarked() = preferenceStore.getLong(
        "default_episode_filter_by_bookmarked",
        Anime.SHOW_ALL,
    )

    // AM (FILLERMARK) -->
    fun filterEpisodeByFillermarked() = preferenceStore.getLong(
        "default_episode_filter_by_fillermarked",
        Anime.SHOW_ALL,
    )
    // <-- AM (FILLERMARK)

    // and upload date
    fun sortEpisodeBySourceOrNumber() = preferenceStore.getLong(
        "default_episode_sort_by_source_or_number",
        Anime.EPISODE_SORTING_SOURCE,
    )

    fun displayEpisodeByNameOrNumber() = preferenceStore.getLong(
        "default_episode_display_by_name_or_number",
        Anime.EPISODE_DISPLAY_NAME,
    )

    fun sortEpisodeByAscendingOrDescending() = preferenceStore.getLong(
        "default_episode_sort_by_ascending_or_descending",
        Anime.EPISODE_SORT_DESC,
    )

    fun setEpisodeSettingsDefault(anime: Anime) {
        filterEpisodeBySeen().set(anime.unseenFilterRaw)
        filterEpisodeByDownloaded().set(anime.downloadedFilterRaw)
        filterEpisodeByBookmarked().set(anime.bookmarkedFilterRaw)
        // AM (FILLERMARK) -->
        filterEpisodeByFillermarked().set(anime.fillermarkedFilterRaw)
        // <-- AM (FILLERMARK)
        sortEpisodeBySourceOrNumber().set(anime.sorting)
        displayEpisodeByNameOrNumber().set(anime.displayMode)
        sortEpisodeByAscendingOrDescending().set(
            if (anime.sortDescending()) Anime.EPISODE_SORT_DESC else Anime.EPISODE_SORT_ASC,
        )
    }

    fun hideMissingEpisodes() = preferenceStore.getBoolean("pref_hide_missing_episode_indicators", false)
    // endregion

    // AY -->
    // Seasons

    fun filterSeasonByDownload() =
        preferenceStore.getLong("default_season_filter_by_downloaded", Anime.SHOW_ALL)

    fun filterSeasonByUnseen() =
        preferenceStore.getLong("default_season_filter_by_unseen", Anime.SHOW_ALL)

    fun filterSeasonByStarted() =
        preferenceStore.getLong("default_season_filter_by_started", Anime.SHOW_ALL)

    fun filterSeasonByBookmarked() =
        preferenceStore.getLong("default_season_filter_by_bookmarked", Anime.SHOW_ALL)

    fun filterSeasonByCompleted() =
        preferenceStore.getLong("default_season_filter_by_completed", Anime.SHOW_ALL)

    fun sortSeasonBySourceOrNumber() = preferenceStore.getLong(
        "default_season_sort_by_source_or_number",
        Anime.SEASON_SORT_SOURCE,
    )

    fun sortSeasonByAscendingOrDescending() = preferenceStore.getLong(
        "default_season_sort_by_ascending_or_descending",
        Anime.SEASON_SORT_DESC,
    )

    fun seasonDisplayGridMode() = preferenceStore.getLong(
        "default_season_grid_display_mode",
        SeasonDisplayMode.toLong(SeasonDisplayMode.CompactGrid),
    )

    fun seasonDisplayGridSize() = preferenceStore.getInt(
        "default_season_grid_display_size",
        0,
    )

    fun seasonDownloadOverlay() = preferenceStore.getBoolean(
        "default_season_download_overlay",
        false,
    )

    fun seasonUnseenOverlay() = preferenceStore.getBoolean(
        "default_season_unseen_overlay",
        true,
    )

    fun seasonLocalOverlay() = preferenceStore.getBoolean(
        "default_season_local_overlay",
        true,
    )

    fun seasonLangOverlay() = preferenceStore.getBoolean(
        "default_season_lang_overlay",
        false,
    )

    fun seasonContinueOverlay() = preferenceStore.getBoolean(
        "default_season_continue_overlay",
        true,
    )

    fun seasonDisplayMode() = preferenceStore.getLong(
        "default_season_display_mode",
        Anime.SEASON_DISPLAY_MODE_SOURCE,
    )

    fun setSeasonSettingsDefault(anime: Anime) {
        filterSeasonByDownload().set(anime.seasonUnseenFilterRaw)
        filterSeasonByUnseen().set(anime.seasonUnseenFilterRaw)
        filterSeasonByStarted().set(anime.seasonStartedFilterRaw)
        filterSeasonByBookmarked().set(anime.seasonBookmarkedFilterRaw)
        filterSeasonByCompleted().set(anime.seasonCompletedFilterRaw)
        sortSeasonBySourceOrNumber().set(anime.seasonSorting)
        sortSeasonByAscendingOrDescending().set(
            if (anime.seasonSortDescending()) Anime.SEASON_SORT_DESC else Anime.SEASON_SORT_ASC,
        )
        seasonDisplayGridMode().set(SeasonDisplayMode.toLong(anime.seasonDisplayGridMode))
        seasonDisplayGridSize().set(anime.seasonDisplayGridSize)
        seasonDownloadOverlay().set(anime.seasonDownloadedOverlay)
        seasonUnseenOverlay().set(anime.seasonUnseenOverlay)
        seasonLocalOverlay().set(anime.seasonLocalOverlay)
        seasonLangOverlay().set(anime.seasonLangOverlay)
        seasonContinueOverlay().set(anime.seasonContinueOverlay)
        seasonDisplayMode().set(anime.seasonDisplayMode)
    }

    // Season behavior

    fun updateSeasonOnRefresh() =
        preferenceStore.getBoolean("pref_update_season_on_refresh", false)

    fun updateSeasonOnLibraryUpdate() =
        preferenceStore.getBoolean("pref_update_season_on_library_update", false)
    // <-- AY

    // region Swipe Actions

    fun swipeToStartAction() = preferenceStore.getEnum(
        "pref_episode_swipe_end_action",
        EpisodeSwipeAction.ToggleBookmark,
    )

    fun swipeToEndAction() = preferenceStore.getEnum(
        "pref_episode_swipe_start_action",
        EpisodeSwipeAction.ToggleSeen,
    )

    fun updateAnimeTitles() = preferenceStore.getBoolean("pref_update_library_anime_titles", false)

    // endregion

    enum class EpisodeSwipeAction {
        ToggleSeen,
        ToggleBookmark,

        // AM (FILLERMARK) -->
        ToggleFillermark,

        // <-- AM (FILLERMARK)
        Download,
        Disabled,
    }

    // AM (GROUPING) -->
    fun groupLibraryUpdateType() = preferenceStore.getEnum("group_library_update_type", GroupLibraryMode.GLOBAL)

    fun groupLibraryBy() = preferenceStore.getInt("group_library_by", LibraryGroup.BY_DEFAULT)
    // <-- AM (GROUPING)

    companion object {
        const val DEVICE_ONLY_ON_WIFI = "wifi"
        const val DEVICE_NETWORK_NOT_METERED = "network_not_metered"
        const val DEVICE_CHARGING = "ac"

        const val ANIME_NON_COMPLETED = "anime_ongoing"
        const val ANIME_HAS_UNSEEN = "anime_fully_seen"
        const val ANIME_NON_SEEN = "anime_started"
        const val ANIME_OUTSIDE_RELEASE_PERIOD = "anime_outside_release_period"

        const val MARK_DUPLICATE_EPISODE_SEEN_NEW = "new"
        const val MARK_DUPLICATE_EPISODE_SEEN_EXISTING = "existing"

        const val DEFAULT_CATEGORY_PREF_KEY = "default_category"
        private const val LIBRARY_UPDATE_CATEGORIES_PREF_KEY = "library_update_categories"
        private const val LIBRARY_UPDATE_CATEGORIES_EXCLUDE_PREF_KEY = "library_update_categories_exclude"
        val categoryPreferenceKeys = setOf(
            DEFAULT_CATEGORY_PREF_KEY,
            LIBRARY_UPDATE_CATEGORIES_PREF_KEY,
            LIBRARY_UPDATE_CATEGORIES_EXCLUDE_PREF_KEY,
        )
    }
}
