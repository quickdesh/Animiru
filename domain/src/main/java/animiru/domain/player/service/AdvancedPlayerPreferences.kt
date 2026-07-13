package animiru.domain.player.service

import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

class AdvancedPlayerPreferences(
    preferenceStore: PreferenceStore,
) {
    val mpvUserFiles: Preference<Boolean> = preferenceStore.getBoolean("mpv_scripts", false)
    val mpvConf: Preference<String> = preferenceStore.getString("pref_mpv_conf", "")
    val mpvInput: Preference<String> = preferenceStore.getString("pref_mpv_input", "")

    // Non-preference

    val playerStatisticsPage: Preference<Int> = preferenceStore.getInt("pref_player_statistics_page", 0)
}
