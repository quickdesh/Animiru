package eu.kanade.tachiyomi.ui.player.components

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import `is`.xyz.mpv.MPV

// Reference: https://github.com/MakD/AFinity/blob/master/app/src/main/java/com/makd/afinity/ui/player/components/MpvSurface.kt
@Composable
fun MpvSurface(
    modifier: Modifier = Modifier,
    mpv: MPV,
    videoOutput: String,
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            SurfaceView(context).apply {
                holder.addCallback(
                    object : SurfaceHolder.Callback {
                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int,
                        ) {
                            mpv.setPropertyString("android-surface-size", "${width}x$height")
                        }

                        override fun surfaceCreated(holder: SurfaceHolder) {
                            mpv.attachSurface(holder.surface)
                            mpv.setOptionString("force-window", "yes")
                            mpv.setPropertyString("vo", videoOutput)
                            mpv.setOptionString("vid", "auto")
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            mpv.setPropertyString("vid", "no")
                            mpv.setPropertyString("vo", "null")
                            mpv.setPropertyString("force-window", "no")
                            mpv.detachSurface()
                        }
                    },
                )
            }
        },
    )
}
