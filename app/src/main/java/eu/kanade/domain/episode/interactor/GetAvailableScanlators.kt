package eu.kanade.domain.episode.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tachiyomi.domain.episode.repository.EpisodeRepository

class GetAvailableScanlators(
    private val repository: EpisodeRepository,
) {

    private fun List<String>.cleanupAvailableScanlators(): Set<String> {
        return mapNotNull { it.ifBlank { null } }.toSet()
    }

    suspend fun await(animeId: Long): Set<String> {
        return repository.getScanlatorsByAnimeId(animeId)
            .cleanupAvailableScanlators()
    }

    fun subscribe(animeId: Long): Flow<Set<String>> {
        return repository.getScanlatorsByAnimeIdAsFlow(animeId)
            .map { it.cleanupAvailableScanlators() }
    }
}
