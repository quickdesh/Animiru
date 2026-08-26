// AY -->
package eu.kanade.presentation.anime.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import aniyomi.domain.anime.model.SeasonAnime
import aniyomi.domain.anime.model.SeasonDisplayMode
import eu.kanade.presentation.library.components.AnimeComfortableGridItem
import eu.kanade.presentation.library.components.AnimeCompactGridItem
import eu.kanade.presentation.library.components.AnimeListItem
import eu.kanade.presentation.library.components.DownloadsBadge
import eu.kanade.presentation.library.components.LanguageBadge
import eu.kanade.presentation.library.components.UnseenBadge
import eu.kanade.presentation.util.formatEpisodeNumber
import eu.kanade.tachiyomi.ui.anime.AnimeSeasonItem
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.model.AnimeCover
import tachiyomi.i18n.aniyomi.AYMR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun AnimeSeasonListItem(
    anime: Anime,
    item: AnimeSeasonItem,
    containerHeight: Int,
    onSeasonClicked: (SeasonAnime) -> Unit,
    onClickContinueWatching: ((SeasonAnime) -> Unit)?,
    listItemModifier: Modifier = Modifier,
) {
    val itemAnime = item.seasonAnime.anime
    val title = if (anime.seasonDisplayMode == Anime.SEASON_DISPLAY_MODE_NUMBER) {
        stringResource(
            AYMR.strings.display_mode_season,
            formatEpisodeNumber(itemAnime.seasonNumber),
        )
    } else {
        itemAnime.title
    }

    when (anime.seasonDisplayGridMode) {
        SeasonDisplayMode.ComfortableGrid -> {
            AnimeComfortableGridItem(
                title = title,
                coverData = AnimeCover(
                    animeId = itemAnime.id,
                    sourceId = itemAnime.source,
                    isAnimeFavorite = itemAnime.favorite,
                    url = itemAnime.thumbnailUrl,
                    lastModified = itemAnime.coverLastModified,
                ),
                coverBadgeStart = {
                    DownloadsBadge(count = item.downloadCount.toInt())
                    UnseenBadge(count = item.unseenCount)
                },
                coverBadgeEnd = {
                    LanguageBadge(
                        isLocal = item.isLocal,
                        sourceLanguage = item.sourceLanguage,
                    )
                },
                onLongClick = { onSeasonClicked(item.seasonAnime) },
                onClick = { onSeasonClicked(item.seasonAnime) },
                onClickContinueWatching = if (onClickContinueWatching != null && item.showContinueOverlay) {
                    { onClickContinueWatching(item.seasonAnime) }
                } else {
                    null
                },
            )
        }
        SeasonDisplayMode.CompactGrid, SeasonDisplayMode.CoverOnlyGrid -> {
            AnimeCompactGridItem(
                title = title.takeIf { anime.seasonDisplayGridMode is SeasonDisplayMode.CompactGrid },
                coverData = AnimeCover(
                    animeId = itemAnime.id,
                    sourceId = itemAnime.source,
                    isAnimeFavorite = itemAnime.favorite,
                    url = itemAnime.thumbnailUrl,
                    lastModified = itemAnime.coverLastModified,
                ),
                coverBadgeStart = {
                    DownloadsBadge(count = item.downloadCount.toInt())
                    UnseenBadge(count = item.unseenCount)
                },
                coverBadgeEnd = {
                    LanguageBadge(
                        isLocal = item.isLocal,
                        sourceLanguage = item.sourceLanguage,
                    )
                },
                onLongClick = { onSeasonClicked(item.seasonAnime) },
                onClick = { onSeasonClicked(item.seasonAnime) },
                onClickContinueWatching = if (onClickContinueWatching != null && item.showContinueOverlay) {
                    { onClickContinueWatching(item.seasonAnime) }
                } else {
                    null
                },
            )
        }
        SeasonDisplayMode.List -> {
            AnimeListItem(
                title = title,
                coverData = AnimeCover(
                    animeId = itemAnime.id,
                    sourceId = itemAnime.source,
                    isAnimeFavorite = itemAnime.favorite,
                    url = itemAnime.thumbnailUrl,
                    lastModified = itemAnime.coverLastModified,
                ),
                badge = {
                    DownloadsBadge(count = item.downloadCount.toInt())
                    UnseenBadge(count = item.unseenCount)
                    LanguageBadge(
                        isLocal = item.isLocal,
                        sourceLanguage = item.sourceLanguage,
                    )
                },
                onLongClick = { onSeasonClicked(item.seasonAnime) },
                onClick = { onSeasonClicked(item.seasonAnime) },
                onClickContinueWatching = if (onClickContinueWatching != null && item.showContinueOverlay) {
                    { onClickContinueWatching(item.seasonAnime) }
                } else {
                    null
                },
                entries = anime.seasonDisplayGridSize,
                containerHeight = containerHeight,
                modifier = listItemModifier,
            )
        }
    }
}
// <-- AY
