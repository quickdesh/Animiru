package eu.kanade.tachiyomi.ui.player.controls.components.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.kanade.presentation.player.components.ExpandableCard
import eu.kanade.presentation.player.components.SliderItem
import eu.kanade.tachiyomi.ui.player.VideoFilters
import eu.kanade.tachiyomi.ui.player.controls.CARDS_MAX_WIDTH
import eu.kanade.tachiyomi.ui.player.controls.panelCardsColors
import eu.kanade.tachiyomi.ui.player.settings.DecoderPreferences
import `is`.xyz.mpv.MPVLib
import tachiyomi.core.common.preference.deleteAndGet
import tachiyomi.i18n.MR
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
fun VideoSettingsFiltersCard(
    modifier: Modifier = Modifier,
) {
    val decoderPreferences = remember { Injekt.get<DecoderPreferences>() }
    var isExpanded by remember { mutableStateOf(true) }

    ExpandableCard(
        isExpanded = isExpanded,
        onExpand = { isExpanded = !isExpanded },
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
            ) {
                Icon(Icons.Default.Tune, null)
                Text(stringResource(AYMR.strings.player_sheets_filters_title))
            }
        },
        colors = panelCardsColors(),
        modifier = modifier.widthIn(max = CARDS_MAX_WIDTH),
    ) {
        Column {
            TextButton(
                onClick = {
                    VideoFilters.entries.forEach {
                        MPVLib.setPropertyInt(it.mpvProperty, it.preference(decoderPreferences).deleteAndGet())
                    }
                },
            ) {
                Text(text = stringResource(MR.strings.action_reset))
            }

            VideoFilters.entries.forEach { filter ->
                val value by filter.preference(decoderPreferences).collectAsState()
                SliderItem(
                    label = stringResource(filter.titleRes),
                    value = value,
                    valueText = value.toString(),
                    onChange = {
                        filter.preference(decoderPreferences).set(it)
                        MPVLib.setPropertyInt(filter.mpvProperty, it)
                    },
                    max = 100,
                    min = -100,
                )
            }

            if (!decoderPreferences.gpuNext().get()) {
                Column(
                    modifier = Modifier
                        .padding(MaterialTheme.padding.medium)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Icon(Icons.Outlined.Info, null)
                    Text(stringResource(AYMR.strings.player_sheets_filters_warning))
                }
            }
        }
    }
}
