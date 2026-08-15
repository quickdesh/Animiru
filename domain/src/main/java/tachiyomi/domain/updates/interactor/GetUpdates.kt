package tachiyomi.domain.updates.interactor

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.updates.model.UpdatesWithRelations
import tachiyomi.domain.updates.repository.UpdatesRepository
import kotlin.time.Instant

class GetUpdates(
    private val repository: UpdatesRepository,
) {

    suspend fun await(seen: Boolean, after: Long): List<UpdatesWithRelations> {
        return repository.awaitWithSeen(seen, after, limit = 500)
    }

    fun subscribe(
        instant: Instant,
        unseen: Boolean?,
        started: Boolean?,
        bookmarked: Boolean?,
        fillermarked: Boolean?,
        hideExcludedScanlators: Boolean,
        includedCategories: List<Long>,
        excludedCategories: List<Long>,
    ): Flow<List<UpdatesWithRelations>> {
        return repository.subscribeAll(
            instant.toEpochMilliseconds(),
            limit = 500,
            unseen = unseen,
            started = started,
            bookmarked = bookmarked,
            fillermarked = fillermarked,
            hideExcludedScanlators = hideExcludedScanlators,
            includedCategories = includedCategories,
            excludedCategories = excludedCategories,
        )
    }

    fun subscribe(seen: Boolean, after: Long): Flow<List<UpdatesWithRelations>> {
        return repository.subscribeWithSeen(seen, after, limit = 500)
    }
}
