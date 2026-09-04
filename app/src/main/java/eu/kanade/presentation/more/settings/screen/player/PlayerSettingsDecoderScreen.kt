package eu.kanade.presentation.more.settings.screen.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import animiru.domain.player.model.Debanding
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.screen.SearchableSettings
import mihon.app.di.appGraph
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.stringResource

object PlayerSettingsDecoderScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = AYMR.strings.pref_player_decoder

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val decoderPreferences = remember { context.appGraph.decoderPreferences }

        val tryHw = decoderPreferences.tryHWDecoding
        val useGpuNext = decoderPreferences.gpuNext
        val debanding = decoderPreferences.debanding
        val yuv420p = decoderPreferences.useYUV420P

        return listOf(
            Preference.PreferenceItem.SwitchPreference(
                preference = tryHw,
                title = stringResource(AYMR.strings.pref_try_hw),
            ),
            Preference.PreferenceItem.SwitchPreference(
                preference = useGpuNext,
                title = stringResource(AYMR.strings.pref_gpu_next_title),
                subtitle = stringResource(AYMR.strings.pref_gpu_next_subtitle),
            ),
            Preference.PreferenceItem.ListPreference(
                preference = debanding,
                entries = Debanding.entries.associateWith {
                    it.name
                    // stringResource(it.)
                },
                title = stringResource(AYMR.strings.pref_debanding_title),
            ),
            Preference.PreferenceItem.SwitchPreference(
                preference = yuv420p,
                title = stringResource(AYMR.strings.pref_use_yuv420p_title),
                subtitle = stringResource(AYMR.strings.pref_use_yuv420p_subtitle),
            ),
        )
    }
}
