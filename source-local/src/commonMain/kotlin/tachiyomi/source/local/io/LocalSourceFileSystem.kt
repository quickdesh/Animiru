package tachiyomi.source.local.io

import com.hippo.unifile.UniFile

expect class LocalSourceFileSystem {

    fun getBaseDirectory(): UniFile?

    fun getFilesInBaseDirectory(): List<UniFile>

    fun getAnimeDirectory(name: String): UniFile?

    fun getFilesInAnimeDirectory(name: String): List<UniFile>
}
