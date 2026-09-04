package eu.kanade.domain.track.service

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.domain.track.model.AutoTrackState
import eu.kanade.tachiyomi.data.track.Tracker
import eu.kanade.tachiyomi.data.track.anilist.Anilist
import eu.kanade.tachiyomi.data.track.kitsu.Kitsu
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.preference.getEnum

@Inject
@SingleIn(AppScope::class)
class TrackPreferences(
    private val preferenceStore: PreferenceStore,
) {

    fun trackUsername(tracker: Tracker) = preferenceStore.getString(
        Preference.privateKey("pref_animesync_username_${tracker.id}"),
        "",
    )

    fun trackDisplayUsername(tracker: Tracker) = preferenceStore.getString(
        Preference.privateKey("pref_mangasync_displayname_${tracker.id}"),
        "",
    )

    fun trackPassword(tracker: Tracker) = preferenceStore.getString(
        Preference.privateKey("pref_animesync_password_${tracker.id}"),
        "",
    )

    fun trackAuthExpired(tracker: Tracker) = preferenceStore.getBoolean(
        Preference.privateKey("pref_tracker_auth_expired_${tracker.id}"),
        false,
    )

    fun setCredentials(tracker: Tracker, username: String, password: String) {
        trackUsername(tracker).set(username)
        trackPassword(tracker).set(password)
        trackAuthExpired(tracker).set(false)
    }

    fun trackToken(tracker: Tracker) = preferenceStore.getString(Preference.privateKey("track_token_${tracker.id}"), "")

    val anilistScoreType: Preference<String> = preferenceStore.getString("anilist_score_type", Anilist.POINT_10)

    val kitsuScoreType: Preference<String> = preferenceStore.getString("kitsu_score_type", Kitsu.RATING_ADVANCED)

    val autoUpdateTrack: Preference<Boolean> = preferenceStore.getBoolean("pref_auto_update_anime_sync_key", true)

    // AY -->
    val trackOnAddingToLibrary: Preference<Boolean> = preferenceStore.getBoolean("track_on_adding_to_library", true)

    val showNextEpisodeAiringTime: Preference<Boolean> = preferenceStore.getBoolean(
        "show_next_episode_airing_time",
        true,
    )
    // <-- AY

    // AM -->
    val syncEnhancedTrackers: Preference<Boolean> = preferenceStore.getBoolean("sync_enhanced_trackers", true)

    val smartTrackerSync: Preference<Boolean> = preferenceStore.getBoolean("smart_sync_trackers", true)
    // <-- AM

    val autoUpdateTrackOnMarkSeen: Preference<AutoTrackState> = preferenceStore.getEnum(
        "pref_auto_update_anime_on_mark_seen",
        AutoTrackState.ALWAYS,
    )
}
