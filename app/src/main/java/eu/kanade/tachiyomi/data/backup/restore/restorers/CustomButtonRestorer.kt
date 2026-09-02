// AY -->
package eu.kanade.tachiyomi.data.backup.restore.restorers

import app.cash.sqldelight.async.coroutines.awaitAsOne
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.backup.models.BackupCustomButtons
import tachiyomi.data.Database
import tachiyomi.domain.custombutton.interactor.GetCustomButtons

@Inject
class CustomButtonRestorer(
    private val database: Database,
    private val getCustomButtons: GetCustomButtons,
) {
    suspend operator fun invoke(
        backupCustomButtons: List<BackupCustomButtons>,
    ) {
        if (backupCustomButtons.isNotEmpty()) {
            val dbCustomButtons = getCustomButtons.getAll()
            val dbCustomButtonsByName = dbCustomButtons.associateBy { it.name }
            var nextSortIndex = dbCustomButtons.maxOfOrNull { it.sortIndex }?.plus(1) ?: 0
            val dbHasFavorite = dbCustomButtons.firstOrNull { it.isFavorite } != null

            backupCustomButtons
                .sortedBy { it.sortIndex }
                .map {
                    val dbCustomButton = dbCustomButtonsByName[it.name]
                    if (dbCustomButton != null) return@map dbCustomButton
                    val sortIndex = nextSortIndex++
                    val isFavorite = it.isFavorite && !dbHasFavorite
                    database.custom_buttonsQueries.insertReturningId(
                        it.name,
                        isFavorite,
                        sortIndex,
                        it.content,
                        it.longPressContent,
                        it.onStartup,
                    )
                        .awaitAsOne()
                }
        }
    }
}
// <-- AY
