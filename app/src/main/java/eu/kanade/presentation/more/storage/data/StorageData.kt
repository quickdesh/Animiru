// AM (STORAGE_SCREEN) -->
package eu.kanade.presentation.more.storage.data

import androidx.compose.ui.graphics.Color
import tachiyomi.domain.anime.model.Anime

data class StorageData(
    val anime: Anime,
    val categories: List<Long>,
    val size: Long,
    val episodeCount: Int,
    val color: Color,
)
// <-- AM (STORAGE_SCREEN)
