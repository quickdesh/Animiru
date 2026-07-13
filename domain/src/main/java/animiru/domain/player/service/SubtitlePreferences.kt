package animiru.domain.player.service

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import animiru.domain.player.model.SubtitleJustification
import animiru.domain.player.model.SubtitlesBorderStyle
import dev.icerock.moko.resources.StringResource
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.preference.getEnum
import tachiyomi.i18n.animiru.AMMR

class SubtitlePreferences(
    preferenceStore: PreferenceStore,
) {
    val preferredSubLanguages: Preference<String> = preferenceStore.getString("pref_subtitle_lang", "")
    val subtitleWhitelist: Preference<String> = preferenceStore.getString("pref_subtitle_whitelist", "")
    val subtitleBlacklist: Preference<String> = preferenceStore.getString("pref_subtitle_blacklist", "")
    val subtitleBlackBars: Preference<Boolean> = preferenceStore.getBoolean("pref_subtitle_black_bars", false)
    val subtitleSystemFonts: Preference<Boolean> = preferenceStore.getBoolean("pref_subtitle_system_fonts", false)

    // Non-preferences

    val screenshotSubtitles: Preference<Boolean> = preferenceStore.getBoolean("pref_screenshot_subtitles", false)

    val subtitleFont: Preference<String> = preferenceStore.getString("pref_subtitle_font", "Sans Serif")
    val subtitleFontSize: Preference<Int> = preferenceStore.getInt("pref_subtitles_font_size", 55)
    val subtitleFontScale: Preference<Float> = preferenceStore.getFloat("pref_sub_scale", 1f)
    val subtitleBorderSize: Preference<Int> = preferenceStore.getInt("pref_sub_border_size", 3)
    val boldSubtitles: Preference<Boolean> = preferenceStore.getBoolean("pref_bold_subtitles", false)
    val italicSubtitles: Preference<Boolean> = preferenceStore.getBoolean("pref_italic_subtitles", false)

    val textColorSubtitles: Preference<Int> = preferenceStore.getInt("pref_text_color_subtitles", Color.White.toArgb())

    val borderColorSubtitles: Preference<Int> = preferenceStore.getInt(
        "pref_border_color_subtitles",
        Color.Black.toArgb(),
    )
    val borderStyleSubtitles: Preference<SubtitlesBorderStyle> = preferenceStore.getEnum(
        "pref_border_style_subtitles",
        SubtitlesBorderStyle.OutlineAndShadow,
    )
    val shadowOffsetSubtitles: Preference<Int> = preferenceStore.getInt("sub_shadow_offset", 0)
    val backgroundColorSubtitles: Preference<Int> = preferenceStore.getInt(
        "pref_background_color_subtitles",
        Color.Transparent.toArgb(),
    )

    val subtitleJustification: Preference<SubtitleJustification> = preferenceStore.getEnum(
        "pref_sub_justify",
        SubtitleJustification.Auto,
    )
    val subtitlePos: Preference<Int> = preferenceStore.getInt("pref_sub_pos", 100)

    val overrideSubsASS: Preference<SubtitleAssOverride> = preferenceStore.getEnum(
        "pref_override_subtitles_ass_enum",
        SubtitleAssOverride.No,
    )

    val subtitlesDelay: Preference<Int> = preferenceStore.getInt("pref_subtitles_delay", 0)
    val subtitlesSpeed: Preference<Float> = preferenceStore.getFloat("pref_subtitles_speed", 1f)
    val subtitlesSecondaryDelay: Preference<Int> = preferenceStore.getInt("pref_subtitles_secondary_delay", 0)
}

enum class SubtitleAssOverride(
    val value: String,
    val titleRes: StringResource,
) {
    No("no", AMMR.strings.player_sheets_subtitles_ass_no),
    Yes("yes", AMMR.strings.player_sheets_subtitles_ass_yes),
    Scale("scale", AMMR.strings.player_sheets_subtitles_ass_scale),
    Force("force", AMMR.strings.player_sheets_subtitles_ass_force),
    Strip("strip", AMMR.strings.player_sheets_subtitles_ass_strip),
    ;

    companion object {
        fun byValue(value: String): SubtitleAssOverride {
            return when (value) {
                "strip" -> Strip
                "force" -> Force
                "scale" -> Scale
                "yes" -> Yes
                else -> No
            }
        }
    }
}
