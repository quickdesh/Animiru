package tachiyomi.domain.anime.model

data class AnimeWithEpisodeCount(
    val anime: Anime,
    val episodeCount: Long,
)
