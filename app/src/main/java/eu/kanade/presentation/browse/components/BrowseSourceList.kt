package eu.kanade.presentation.browse.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import eu.kanade.presentation.library.components.AnimeListItem
import eu.kanade.presentation.library.components.CommonAnimeItemDefaults
import kotlinx.coroutines.flow.StateFlow
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.model.AnimeCover
import tachiyomi.presentation.core.util.plus

@Composable
fun BrowseSourceList(
    animeList: LazyPagingItems<StateFlow<Anime>>,
    // AY -->
    entries: Int,
    topBarHeight: Int,
    // <-- AY
    contentPadding: PaddingValues,
    onAnimeClick: (Anime) -> Unit,
    onAnimeLongClick: (Anime) -> Unit,
) {
    // AY -->
    var containerHeight by remember { mutableIntStateOf(0) }
    // <-- AY
    LazyColumn(
        contentPadding = contentPadding + PaddingValues(vertical = 8.dp),
        // AY -->
        modifier = Modifier
            .onGloballyPositioned { layoutCoordinates ->
                containerHeight = layoutCoordinates.size.height - topBarHeight
            },
        // <-- AY
    ) {
        item {
            if (animeList.loadState.prepend is LoadState.Loading) {
                BrowseSourceLoadingItem()
            }
        }

        items(count = animeList.itemCount) { index ->
            val anime by animeList[index]?.collectAsState() ?: return@items
            BrowseSourceListItem(
                anime = anime,
                onClick = { onAnimeClick(anime) },
                onLongClick = { onAnimeLongClick(anime) },
                // AY -->
                entries = entries,
                containerHeight = containerHeight,
                // <-- AY
            )
        }

        item {
            if (animeList.loadState.refresh is LoadState.Loading || animeList.loadState.append is LoadState.Loading) {
                BrowseSourceLoadingItem()
            }
        }
    }
}

@Composable
private fun BrowseSourceListItem(
    anime: Anime,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = onClick,
    // AY -->
    entries: Int,
    containerHeight: Int,
    // <-- AY
) {
    AnimeListItem(
        title = anime.title,
        coverData = AnimeCover(
            animeId = anime.id,
            sourceId = anime.source,
            isAnimeFavorite = anime.favorite,
            url = anime.thumbnailUrl,
            lastModified = anime.coverLastModified,
        ),
        coverAlpha = if (anime.favorite) CommonAnimeItemDefaults.BrowseFavoriteCoverAlpha else 1f,
        badge = {
            InLibraryBadge(enabled = anime.favorite)
        },
        onLongClick = onLongClick,
        onClick = onClick,
        // AY -->
        entries = entries,
        containerHeight = containerHeight,
        // <-- AY
    )
}
