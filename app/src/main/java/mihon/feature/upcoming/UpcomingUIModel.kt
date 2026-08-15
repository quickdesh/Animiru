package mihon.feature.upcoming

import kotlinx.datetime.LocalDate
import tachiyomi.domain.anime.model.Anime

sealed interface UpcomingUIModel {
    data class Header(val date: LocalDate, val animeCount: Int) : UpcomingUIModel
    data class Item(val anime: Anime) : UpcomingUIModel
}
