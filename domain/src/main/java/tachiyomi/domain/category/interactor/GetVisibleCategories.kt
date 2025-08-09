package tachiyomi.domain.category.interactor

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.repository.CategoryRepository

class GetVisibleCategories(
    private val categoryRepository: CategoryRepository,
) {
    fun subscribe(): Flow<List<Category>> {
        return categoryRepository.getAllVisibleAsFlow()
    }

    fun subscribe(animeId: Long): Flow<List<Category>> {
        return categoryRepository.getVisibleCategoriesByAnimeIdAsFlow(animeId)
    }

    suspend fun await(): List<Category> {
        return categoryRepository.getAllVisible()
    }

    suspend fun await(animeId: Long): List<Category> {
        return categoryRepository.getVisibleCategoriesByAnimeId(animeId)
    }
}
