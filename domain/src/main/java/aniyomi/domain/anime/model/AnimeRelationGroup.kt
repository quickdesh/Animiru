package aniyomi.domain.anime.model

import tachiyomi.domain.anime.model.Anime

data class AnimeRelationGroup(
    val name: String,
    val anime: List<Anime>,
)
