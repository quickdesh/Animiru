package tachiyomi.cast

import android.content.Context
import eu.kanade.tachiyomi.animesource.model.Video
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

class CastManagerImpl : CastManager {

    override val castState = MutableStateFlow(CastState())

    override val castEvent = MutableSharedFlow<CastEvent>()

    override fun initialize(context: Context) {
        // NOOP
    }

    override fun startCasting(video: Video, startPosition: Long) {
        // NOOP
    }
}
