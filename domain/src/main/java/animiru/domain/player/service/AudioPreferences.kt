package animiru.domain.player.service

import animiru.domain.player.model.AudioChannels
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.preference.getEnum

@Inject
@SingleIn(AppScope::class)
class AudioPreferences(
    preferenceStore: PreferenceStore,
) {
    val preferredAudioLanguages: Preference<String> = preferenceStore.getString("pref_audio_lang", "")
    val enablePitchCorrection: Preference<Boolean> = preferenceStore.getBoolean("pref_audio_pitch_correction", true)
    val audioChannels: Preference<AudioChannels> = preferenceStore.getEnum("pref_audio_config", AudioChannels.AutoSafe)
    val volumeBoostCap: Preference<Int> = preferenceStore.getInt("pref_audio_volume_boost_cap", 30)

    // Non-preferences

    val audioDelay: Preference<Int> = preferenceStore.getInt("pref_audio_delay", 0)
}
