package mihon.domain.source.interactor.models

import tachiyomi.domain.anime.model.Anime

data class RemoteAnimeSeasonUpdate(
    val anime: Anime,
    val newSeasons: List<Anime>,
)
