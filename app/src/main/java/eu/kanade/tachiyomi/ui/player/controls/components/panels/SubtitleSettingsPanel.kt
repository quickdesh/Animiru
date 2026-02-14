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

package eu.kanade.tachiyomi.ui.player.controls.components.panels

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.tachiyomi.ui.player.controls.components.panels.components.MultiCardPanel
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun SubtitleSettingsPanel(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MultiCardPanel(
        onDismissRequest = onDismissRequest,
        title = stringResource(AYMR.strings.player_sheets_subtitles_settings_title),
        cardCount = 3,
        modifier = modifier,
    ) { index, cardModifier ->
        when (index) {
            0 -> SubtitleSettingsTypographyCard(cardModifier)
            1 -> SubtitleSettingsColorsCard(cardModifier)
            2 -> SubtitlesMiscellaneousCard(cardModifier)
            else -> {}
        }
    }
}
