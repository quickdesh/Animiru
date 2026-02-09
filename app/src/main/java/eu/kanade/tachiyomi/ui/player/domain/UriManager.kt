package eu.kanade.tachiyomi.ui.player.domain

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import eu.kanade.tachiyomi.ui.player.openContentFd

class UriManager(
    private val context: Context,
) {
    fun openContentFd(uri: Uri): String? {
        return uri.openContentFd(context)
    }

    fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    fun getCachePath(): String {
        return context.cacheDir.path
    }
}
