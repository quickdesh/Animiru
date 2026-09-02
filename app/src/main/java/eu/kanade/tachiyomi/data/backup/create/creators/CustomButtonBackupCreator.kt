// AY -->
package eu.kanade.tachiyomi.data.backup.create.creators

import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.backup.models.BackupCustomButtons
import eu.kanade.tachiyomi.data.backup.models.backupCustomButtonsMapper
import tachiyomi.domain.custombutton.interactor.GetCustomButtons

@Inject
class CustomButtonBackupCreator(
    private val getCustomButtons: GetCustomButtons,
) {
    suspend operator fun invoke(): List<BackupCustomButtons> {
        return getCustomButtons.getAll()
            .map(backupCustomButtonsMapper)
    }
}
// <-- AY
