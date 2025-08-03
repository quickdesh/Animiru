package eu.kanade.domain.anime.interactor

import tachiyomi.data.DatabaseHandler

class SetExcludedScanlators(
    private val handler: DatabaseHandler,
) {

    suspend fun await(animeId: Long, excludedScanlators: Set<String>) {
        handler.await(inTransaction = true) {
            val currentExcluded = handler.awaitList {
                excluded_scanlatorsQueries.getExcludedScanlatorsByAnimeId(animeId)
            }.toSet()
            val toAdd = excludedScanlators.minus(currentExcluded)
            for (scanlator in toAdd) {
                excluded_scanlatorsQueries.insert(animeId, scanlator)
            }
            val toRemove = currentExcluded.minus(excludedScanlators)
            excluded_scanlatorsQueries.remove(animeId, toRemove)
        }
    }
}
