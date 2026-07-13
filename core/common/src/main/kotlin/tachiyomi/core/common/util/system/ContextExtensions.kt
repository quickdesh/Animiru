package tachiyomi.core.common.util.system

import android.content.Context
import java.io.File

// AM -->
fun Context.createFileInCacheDir(name: String): File {
    val file = File(externalCacheDir, name)
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    return file
}
// <-- AM
