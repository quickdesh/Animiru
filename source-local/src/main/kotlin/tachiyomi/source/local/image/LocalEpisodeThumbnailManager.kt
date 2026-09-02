// AY -->
package tachiyomi.source.local.image

import android.content.Context
import com.hippo.unifile.UniFile
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.util.storage.DiskUtil
import tachiyomi.core.common.storage.nameWithoutExtension
import tachiyomi.core.common.util.system.ImageUtil
import tachiyomi.source.local.io.LocalSourceFileSystem
import java.io.InputStream

private const val DEFAULT_THUMBNAIL_NAME = "thumbnail.jpg"

@Inject
@SingleIn(AppScope::class)
class LocalEpisodeThumbnailManager(
    private val context: Context,
    private val fileSystem: LocalSourceFileSystem,
) {

    fun find(animeUrl: String, fileName: String): UniFile? {
        return fileSystem.getFilesInAnimeDirectory(animeUrl)
            // Get all file whose names contain the episode name and the word 'thumbnail'
            .filter { it.isFile && it.nameWithoutExtension.equals(fileName, ignoreCase = true) }
            // Get the first actual image
            .firstOrNull { ImageUtil.isImage(it.name) { it.openInputStream() } }
    }

    fun update(anime: SAnime, episode: SEpisode, inputStream: InputStream): UniFile? {
        val directory = fileSystem.getAnimeDirectory(anime.url)
        if (directory == null) {
            inputStream.close()
            return null
        }

        val fileName = "${episode.name}-$DEFAULT_THUMBNAIL_NAME"
        val targetFile = find(anime.url, fileName) ?: directory.createFile(fileName)!!

        inputStream.use { input ->
            targetFile.openOutputStream().use { output ->
                input.copyTo(output)
            }
        }

        DiskUtil.createNoMediaFile(directory, context)

        episode.preview_url = targetFile.uri.toString()
        return targetFile
    }
}

// <-- AY
