// AY -->
package tachiyomi.domain.custombutton.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.custombutton.model.CustomButton
import tachiyomi.domain.custombutton.repository.CustomButtonRepository

@Inject
class GetCustomButtons(
    private val customButtonRepository: CustomButtonRepository,
) {
    fun subscribeAll(): Flow<List<CustomButton>> {
        return customButtonRepository.subscribeAll()
    }

    suspend fun getAll(): List<CustomButton> {
        return customButtonRepository.getAll()
    }
}
// <-- AY
