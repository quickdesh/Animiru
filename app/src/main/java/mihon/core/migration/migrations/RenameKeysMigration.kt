package mihon.core.migration.migrations

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext

@Inject
@ContributesIntoSet(AppScope::class)
class RenameKeysMigration(
    private val context: Context,
) : Migration {
    override val version = 131f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val map = prefs.all

        val prefsToRename = KEYS_TO_RENAME.toMutableMap()

        // Add mangasync stuff, from TrackPreferences
        listOf("pref_mangasync_username_", "pref_mangasync_password_").forEach {
            val oldKey = map.keys.firstOrNull { k -> k.contains(it) } ?: return@forEach
            val newKey = oldKey.replace("mangasync", "animesync")
            prefsToRename.put(oldKey, newKey)
        }

        // Add tracker filter stuff, from LibraryPreferences
        listOf("pref_filter_animelib_tracked_").forEach {
            val oldKey = map.keys.firstOrNull { k -> k.contains(it) } ?: return@forEach
            val newKey = oldKey.replace("animelib", "library")
            prefsToRename.put(oldKey, newKey)
        }

        prefs.edit {
            prefsToRename.forEach { (old, new) ->
                val value = map[old] ?: return@forEach
                when (value) {
                    is Int -> {
                        putInt(new, value)
                    }
                    is Long -> {
                        putLong(new, value)
                    }
                    is Float -> {
                        putFloat(new, value)
                    }
                    is String -> {
                        putString(new, value)
                    }
                    is Boolean -> {
                        putBoolean(new, value)
                    }
                    is Set<*> -> (value as? Set<String>)?.let {
                        putStringSet(new, value)
                    }
                }
                remove(old)
            }
        }

        return true
    }

    companion object {
        val KEYS_TO_RENAME = mapOf(
            // TrackPreferences
            "pref_auto_update_manga_on_mark_read" to "pref_auto_update_anime_on_mark_seen",
            "pref_auto_update_manga_sync_key" to "pref_auto_update_anime_sync_key",

            // DownloadPreferences
            "remove_after_read_slots" to "remove_after_seen_slots",
            "pref_remove_after_marked_as_read_key" to "pref_remove_after_marked_as_seen_key",
            "download_new_episode" to "download_new",
            "download_new_unread_chapters_only" to "download_new_unseen_episodes_only",
            "remove_exclude_anime_categories" to "remove_exclude_categories",
            "download_new_anime_categories" to "download_new_categories",
            "download_new_anime_categories_exclude" to "download_new_categories_exclude",

            // SourcePreferences
            "anime_extension_repos" to "extension_repos",

            // LibraryPreferences
            "animelib_sorting_mode" to "library_sorting_mode",
            "pref_animelib_columns_portrait_key" to "pref_library_columns_portrait_key",
            "pref_animelib_columns_landscape_key" to "pref_library_columns_landscape_key",
            "library_update_manga_restriction" to "library_update_anime_restriction",
            "display_continue_reading_button" to "display_continue_watching_button",
            "pref_filter_animelib_downloaded_v2" to "pref_filter_library_downloaded_v2",
            "pref_filter_animelib_unread_v2" to "pref_filter_library_unseen_v2",
            "pref_filter_animelib_started_v2" to "pref_filter_library_started_v2",
            "pref_filter_animelib_bookmarked_v2" to "pref_filter_library_bookmarked_v2",
            "pref_filter_animelib_fillermarked_v2" to "pref_filter_library_fillermarked_v2",
            "pref_filter_animelib_completed_v2" to "pref_filter_library_completed_v2",
            "display_unread_badge" to "display_unseen_badge",
            "default_chapter_display_by_name_or_number" to "default_episode_display_by_name_or_number",
            "default_chapter_sort_by_ascending_or_descending" to "default_episode_sort_by_ascending_or_descending",
            "new_episode" to "new",
            "existing_episode" to "existing",
            "default_anime_category" to "default_category",
            "animelib_update_categories" to "library_update_categories",
            "animelib_update_categories_exclude" to "library_update_categories_exclude",
        )
    }
}
