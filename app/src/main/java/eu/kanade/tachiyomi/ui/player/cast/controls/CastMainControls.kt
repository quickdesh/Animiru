package eu.kanade.tachiyomi.ui.player.cast.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.vivvvek.seeker.Seeker
import dev.vivvvek.seeker.SeekerDefaults
import dev.vivvvek.seeker.Segment
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.ui.player.cast.CastUiData
import eu.kanade.tachiyomi.ui.player.cast.components.CastControlButton
import eu.kanade.tachiyomi.ui.player.components.CurrentChapter
import eu.kanade.tachiyomi.ui.player.controls.components.VideoTimer
import tachiyomi.cast.CastState
import tachiyomi.presentation.core.components.material.padding

@Composable
fun CastMainControls(
    castState: CastState,
    castUiData: CastUiData,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            CastControlButton(
                Icons.Filled.SkipPrevious,
                onClick = onPrevious,
                iconSize = 36.dp,
                enabled = hasPrevious,
            )

            CastControlButton(
                Icons.Filled.FastRewind,
                onClick = { },
                iconSize = 36.dp,
                enabled = true,
            )

            if (castState.buffering || castUiData.loading) {
                Box(modifier = Modifier.padding(MaterialTheme.padding.medium)) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                    )
                }
            } else {
                CastControlButton(
                    if (castState.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    onClick = onPlayPause,
                    iconSize = 36.dp,
                    enabled = true,
                )
            }

            CastControlButton(
                Icons.Filled.FastForward,
                onClick = { },
                iconSize = 36.dp,
                enabled = true,
            )

            CastControlButton(
                Icons.Filled.SkipNext,
                onClick = onNext,
                iconSize = 36.dp,
                enabled = hasNext,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(40.dp),
        ) {
            VideoTimer(
                value = castState.position.toFloat(),
                isInverted = false,
                onClick = { },
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(92.dp),
            )

            if (castUiData.showChapterIndicator && castUiData.currentChapter != null) {
                CurrentChapter(
                    chapter = castUiData.currentChapter,
                    background = MaterialTheme.colorScheme.onBackground,
                    onBackground = MaterialTheme.colorScheme.background,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            VideoTimer(
                value = castUiData.duration.toFloat(),
                isInverted = false,
                onClick = {
                    // clickEvent()
                    // positionTimerOnClick()
                },
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(92.dp),
            )
        }

        Seeker(
            value = castState.position.toFloat(),
            range = 0f..castUiData.duration.toFloat(),
            onValueChange = { },
            onValueChangeFinished = { },
            readAheadValue = 0f,
            segments = castUiData.chapters,
            modifier = Modifier,
            colors = SeekerDefaults.seekerColors(
                progressColor = MaterialTheme.colorScheme.primary,
                thumbColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onBackground,
                readAheadColor = MaterialTheme.colorScheme.inversePrimary,
            ),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
        ) {
            CastControlButton(
                icon = Icons.Default.VideoLibrary,
                onClick = { },
                onLongClick = { },
                horizontalSpacing = MaterialTheme.padding.mediumSmall,
            )

            CastControlButton(
                icon = Icons.Default.Speed,
                onClick = { },
                onLongClick = { },
                horizontalSpacing = MaterialTheme.padding.mediumSmall,
            )

            CastControlButton(
                icon = Icons.Default.Subtitles,
                onClick = { },
                onLongClick = { },
                horizontalSpacing = MaterialTheme.padding.mediumSmall,
            )

            CastControlButton(
                icon = Icons.Default.Audiotrack,
                onClick = { },
                onLongClick = { },
                horizontalSpacing = MaterialTheme.padding.mediumSmall,
            )

            CastControlButton(
                icon = Icons.Default.HighQuality,
                onClick = { },
                onLongClick = { },
                horizontalSpacing = MaterialTheme.padding.mediumSmall,
            )

            Button(onClick = {}) {
                Text("+85s")
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
        ) {
            Button(onClick = {}) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Close, null)
                    Text("Stop casting")
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun CastMainControlsPreview() {
    TachiyomiPreviewTheme {
        CastMainControls(
            castState = CastState(),
            castUiData = CastUiData(
                showChapterIndicator = true,
                currentChapter = Segment("Opening", 140f),
            ),
            hasNext = true,
            hasPrevious = false,
            onPlayPause = { },
            onNext = { },
            onPrevious = { },
        )
    }
}
