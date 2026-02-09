package eu.kanade.tachiyomi.ui.player.domain

import android.content.Context
import dev.icerock.moko.resources.StringResource
import tachiyomi.core.common.i18n.stringResource

class StringResourceManager(
    private val context: Context,
) {
    // Use sparsely
    fun get(stringRes: StringResource, vararg args: Any): String {
        return context.stringResource(stringRes, *args)
    }
}
