package tachiyomi.source.local

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.animesource.model.FetchType
import tachiyomi.source.local.io.Format
import tachiyomi.source.local.io.LocalSourceFileSystem

@Inject
@SingleIn(AppScope::class)
class LocalFetchTypeManager(
    private val context: Context,
    private val fileSystem: LocalSourceFileSystem,
) {
    fun find(animeUrl: String): FetchType {
        val files = fileSystem.getFilesInAnimeDirectory(animeUrl)

        return when {
            files.any { Format.isSupported(it) } -> FetchType.Episodes
            files.any { it.isDirectory } -> FetchType.Seasons
            else -> FetchType.Episodes
        }
    }
}
