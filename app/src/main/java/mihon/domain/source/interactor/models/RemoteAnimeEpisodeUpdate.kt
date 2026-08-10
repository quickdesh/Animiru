package mihon.domain.source.interactor.models

import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.episode.model.Episode

data class RemoteAnimeEpisodeUpdate(
    val anime: Anime,
    val newEpisodes: List<Episode>,
)
