package tachiyomi.cast

import android.content.Context
import eu.kanade.tachiyomi.animesource.model.Video
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface CastManager {

    val castState: StateFlow<CastState>

    val castEvent: SharedFlow<CastEvent>

    fun initialize(context: Context)

    fun startCasting(
        video: Video,
        startPosition: Long = 0L,
    )
}
