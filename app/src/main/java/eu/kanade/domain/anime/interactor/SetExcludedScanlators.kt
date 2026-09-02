package eu.kanade.domain.anime.interactor

import app.cash.sqldelight.async.coroutines.awaitAsList
import dev.zacsweers.metro.Inject
import tachiyomi.data.Database

@Inject
class SetExcludedScanlators(
    private val database: Database,
) {

    suspend fun await(animeId: Long, excludedScanlators: Set<String>) {
        database.transaction {
            val currentExcluded = database.excluded_scanlatorsQueries.getExcludedScanlatorsByAnimeId(animeId)
                .awaitAsList().toSet()
            val toAdd = excludedScanlators.minus(currentExcluded)
            for (scanlator in toAdd) {
                database.excluded_scanlatorsQueries.insert(animeId, scanlator)
            }
            val toRemove = currentExcluded.minus(excludedScanlators)
            database.excluded_scanlatorsQueries.remove(animeId, toRemove)
        }
    }
}
