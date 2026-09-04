package eu.kanade.domain.anime.interactor

import app.cash.sqldelight.async.coroutines.awaitAsList
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tachiyomi.data.Database
import tachiyomi.data.subscribeToList

@Inject
class GetExcludedScanlators(
    private val database: Database,
) {

    suspend fun await(animeId: Long): Set<String> {
        return database.excluded_scanlatorsQueries.getExcludedScanlatorsByAnimeId(animeId)
            .awaitAsList()
            .toSet()
    }

    fun subscribe(animeId: Long): Flow<Set<String>> {
        return database.excluded_scanlatorsQueries.getExcludedScanlatorsByAnimeId(animeId)
            .subscribeToList()
            .map { it.toSet() }
    }
}
