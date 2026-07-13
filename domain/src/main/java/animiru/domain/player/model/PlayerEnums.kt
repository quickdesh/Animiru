/*
 * Copyright 2024 Abdallah Mehiz
 * https://github.com/abdallahmehiz/mpvKt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package animiru.domain.player.model

import animiru.domain.player.service.DecoderPreferences
import dev.icerock.moko.resources.StringResource
import tachiyomi.core.common.preference.Preference
import tachiyomi.i18n.MR
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.i18n.aniyomi.AYMR

/**
 * Results of the set as art feature.
 */
enum class SetAsArt {
    Success,
    AddToLibraryFirst,
    Error,
}

enum class ArtType {
    Cover,
    Background,
    Thumbnail,
}

enum class PlayerOrientation(val titleRes: StringResource) {
    Free(MR.strings.rotation_free),
    Video(AYMR.strings.rotation_video),
    Portrait(MR.strings.rotation_portrait),
    ReversePortrait(MR.strings.rotation_reverse_portrait),
    SensorPortrait(AYMR.strings.rotation_sensor_portrait),
    Landscape(MR.strings.rotation_landscape),
    ReverseLandscape(AYMR.strings.rotation_reverse_landscape),
    SensorLandscape(AYMR.strings.rotation_sensor_landscape),
}

enum class VideoAspect(val titleRes: StringResource) {
    Crop(AYMR.strings.video_crop_screen),
    Fit(AYMR.strings.video_fit_screen),
    Stretch(AYMR.strings.video_stretch_screen),
}

/**
 * Action performed by a button, like double tap or media controls
 */
enum class SingleActionGesture(val stringRes: StringResource) {
    None(stringRes = AYMR.strings.single_action_none),
    Seek(stringRes = AYMR.strings.single_action_seek),
    PlayPause(stringRes = AYMR.strings.single_action_playpause),
    Switch(stringRes = AYMR.strings.single_action_switch),
    Custom(stringRes = AYMR.strings.single_action_custom),
}

/**
 * Key codes sent through the `Custom` option in gestures
 */
enum class CustomKeyCodes(val keyCode: String) {
    DoubleTapLeft("0x10001"),
    DoubleTapCenter("0x10002"),
    DoubleTapRight("0x10003"),
    MediaPrevious("0x10004"),
    MediaPlay("0x10005"),
    MediaNext("0x10006"),
}

enum class Decoder(val title: String, val value: String) {
    AutoCopy("Auto", "auto-copy"),
    Auto("Auto", "auto"),
    SW("SW", "no"),
    HW("HW", "mediacodec-copy"),
    HWPlus("HW+", "mediacodec"),
    ;

    companion object {
        fun getDecoderFromValue(value: String): Decoder {
            return entries.first { it.value == value }
        }
    }
}

enum class Debanding(val stringRes: StringResource) {
    None(AMMR.strings.player_sheets_deband_none),
    CPU(AMMR.strings.player_sheets_deband_cpu),
    GPU(AMMR.strings.player_sheets_deband_gpu),
}

enum class VideoFilters(
    val titleRes: StringResource,
    val preference: (DecoderPreferences) -> Preference<Int>,
    val mpvProperty: String,
) {
    BRIGHTNESS(
        AYMR.strings.player_sheets_filters_brightness,
        { it.brightnessFilter },
        "brightness",
    ),
    SATURATION(
        AYMR.strings.player_sheets_filters_Saturation,
        { it.saturationFilter },
        "saturation",
    ),
    CONTRAST(
        AYMR.strings.player_sheets_filters_contrast,
        { it.contrastFilter },
        "contrast",
    ),
    GAMMA(
        AYMR.strings.player_sheets_filters_gamma,
        { it.gammaFilter },
        "gamma",
    ),
    HUE(
        AYMR.strings.player_sheets_filters_hue,
        { it.hueFilter },
        "hue",
    ),
}

enum class DebandSettings(
    val stringRes: StringResource,
    val preference: (DecoderPreferences) -> Preference<Int>,
    val mpvProperty: String,
    val start: Int,
    val end: Int,
) {
    Iterations(
        AMMR.strings.player_sheets_deband_iterations,
        { it.debandIterations },
        "deband-iterations",
        0,
        16,
    ),
    Threshold(
        AMMR.strings.player_sheets_deband_threshold,
        { it.debandThreshold },
        "deband-threshold",
        0,
        200,
    ),
    Range(
        AMMR.strings.player_sheets_deband_range,
        { it.debandRange },
        "deband-range",
        1,
        64,
    ),
    Grain(
        AMMR.strings.player_sheets_deband_grain,
        { it.debandGrain },
        "deband-grain",
        0,
        200,
    ),
}

enum class AudioChannels(val titleRes: StringResource, val property: String, val value: String) {
    Auto(AYMR.strings.pref_player_audio_channels_auto, "audio-channels", "auto-safe"),
    AutoSafe(AYMR.strings.pref_player_audio_channels_auto_safe, "audio-channels", "auto"),
    Mono(AYMR.strings.pref_player_audio_channels_mono, "audio-channels", "mono"),
    Stereo(AYMR.strings.pref_player_audio_channels_stereo, "audio-channels", "stereo"),
    ReverseStereo(AYMR.strings.pref_player_audio_channels_reverse_stereo, "af", "pan=[stereo|c0=c1|c1=c0]"),
}

enum class SubtitleJustification(
    val value: String,
) {
    Left("left"),
    Center("center"),
    Right("right"),
    Auto("auto"),
    ;

    companion object {
        fun byValue(value: String): SubtitleJustification {
            return when (value) {
                "left" -> Left
                "center" -> Center
                "right" -> Right
                else -> Auto
            }
        }
    }
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

enum class SubtitlesBorderStyle(
    val value: String,
    val titleRes: StringResource,
) {
    OutlineAndShadow("outline-and-shadow", AYMR.strings.player_sheets_subtitles_border_style_outline_and_shadow),
    OpaqueBox("opaque-box", AYMR.strings.player_sheets_subtitles_border_style_opaque_box),
    BackgroundBox("background-box", AYMR.strings.player_sheets_subtitles_border_style_background_box),
    ;

    companion object {
        fun byValue(value: String): SubtitlesBorderStyle {
            return when (value) {
                "outline-and-shadow" -> OutlineAndShadow
                "opaque-box" -> OpaqueBox
                "background-box" -> BackgroundBox
                else -> throw IllegalArgumentException("Unsupported border style: $value")
            }
        }
    }
}
