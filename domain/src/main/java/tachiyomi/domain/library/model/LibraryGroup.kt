// AM (GROUPING) -->
package tachiyomi.domain.library.model

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.animiru.AMMR

object LibraryGroup {

    const val BY_DEFAULT = 0
    const val BY_SOURCE = 1
    const val BY_STATUS = 2
    const val BY_TRACK_STATUS = 3
    const val UNGROUPED = 4

    fun groupTypeStringRes(type: Int, hasCategories: Boolean = true): StringResource {
        return when (type) {
            BY_STATUS -> MR.strings.status
            BY_SOURCE -> MR.strings.label_sources
            BY_TRACK_STATUS -> AMMR.strings.tracking_status
            UNGROUPED -> AMMR.strings.ungrouped
            else -> if (hasCategories) MR.strings.categories else AMMR.strings.ungrouped
        }
    }
}
// <-- AM (GROUPING)
