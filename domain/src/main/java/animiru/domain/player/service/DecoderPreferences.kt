package animiru.domain.player.service

import animiru.domain.player.model.Debanding
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.preference.getEnum

class DecoderPreferences(
    preferenceStore: PreferenceStore,
) {
    val tryHWDecoding: Preference<Boolean> = preferenceStore.getBoolean("pref_try_hwdec", true)
    val gpuNext: Preference<Boolean> = preferenceStore.getBoolean("pref_gpu_next", false)
    val useYUV420P: Preference<Boolean> = preferenceStore.getBoolean("use_yuv420p", true)

    val debanding: Preference<Debanding> = preferenceStore.getEnum("pref_video_debanding", Debanding.None)
    val debandIterations: Preference<Int> = preferenceStore.getInt("deband_iterations", 1)
    val debandThreshold: Preference<Int> = preferenceStore.getInt("deband_threshold", 48)
    val debandRange: Preference<Int> = preferenceStore.getInt("deband_range", 16)
    val debandGrain: Preference<Int> = preferenceStore.getInt("deband_grain", 32)

    // Non-preferences

    val brightnessFilter: Preference<Int> = preferenceStore.getInt("pref_player_filter_brightness")
    val saturationFilter: Preference<Int> = preferenceStore.getInt("pref_player_filter_saturation")
    val contrastFilter: Preference<Int> = preferenceStore.getInt("pref_player_filter_contrast")
    val gammaFilter: Preference<Int> = preferenceStore.getInt("pref_player_filter_gamma")
    val hueFilter: Preference<Int> = preferenceStore.getInt("pref_player_filter_hue")
}
