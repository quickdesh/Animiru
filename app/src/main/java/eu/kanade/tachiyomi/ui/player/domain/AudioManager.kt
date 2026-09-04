package eu.kanade.tachiyomi.ui.player.domain

import android.content.Context
import android.media.AudioManager
import dev.zacsweers.metro.Inject

@Inject
class AudioManager(
    private val context: Context,
) {
    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    fun getVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    fun setVolume(volume: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume,
            0,
        )
    }
}
