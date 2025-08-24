package eu.kanade.tachiyomi.data.download

import android.content.Context
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.util.storage.DiskUtil
import eu.kanade.tachiyomi.util.storage.size
import logcat.LogPriority
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.storage.displayablePath
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.episode.model.Episode
import tachiyomi.domain.storage.service.StorageManager
import tachiyomi.i18n.MR
import tachiyomi.source.local.io.LocalSourceFileSystem
import tachiyomi.source.local.isLocal
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.IOException

/**
 * This class is used to provide the directories where the downloads should be saved.
 * It uses the following path scheme: /<root downloads dir>/<source name>/<anime>/<episode>
 *
 * @param context the application context.
 */
class DownloadProvider(
    private val context: Context,
    private val storageManager: StorageManager = Injekt.get(),
    // AM (FILE_SIZE) -->
    private val localFileSystem: LocalSourceFileSystem = Injekt.get(),
    // <-- AM (FILE_SIZE)
) {

    private val downloadsDir: UniFile?
        get() = storageManager.getDownloadsDirectory()

    /**
     * Returns the download directory for an anime. For internal use only.
     *
     * @param animeTitle the title of the anime to query.
     * @param source the source of the anime.
     */
    internal fun getAnimeDir(animeTitle: String, source: AnimeSource): Result<UniFile> {
        val downloadsDir = downloadsDir
        if (downloadsDir == null) {
            logcat(LogPriority.ERROR) { "Failed to create download directory" }
            return Result.failure(
                IOException(context.stringResource(MR.strings.storage_failed_to_create_download_directory)),
            )
        }

        val sourceDirName = getSourceDirName(source)
        val sourceDir = downloadsDir.createDirectory(sourceDirName)
        if (sourceDir == null) {
            val displayablePath = downloadsDir.displayablePath + "/$sourceDirName"
            logcat(LogPriority.ERROR) { "Failed to create source download directory: $displayablePath" }
            return Result.failure(
                IOException(context.stringResource(MR.strings.storage_failed_to_create_directory, displayablePath)),
            )
        }

        val animeDirName = getAnimeDirName(animeTitle)
        val animeDir = sourceDir.createDirectory(animeDirName)
        if (animeDir == null) {
            val displayablePath = sourceDir.displayablePath + "/$animeDirName"
            logcat(LogPriority.ERROR) { "Failed to create anime download directory: $displayablePath" }
            return Result.failure(
                IOException(context.stringResource(MR.strings.storage_failed_to_create_directory, displayablePath)),
            )
        }

        return Result.success(animeDir)
    }

    /**
     * Returns the download directory for a source if it exists.
     *
     * @param source the source to query.
     */
    fun findSourceDir(source: AnimeSource): UniFile? {
        return downloadsDir?.findFile(getSourceDirName(source))
    }

    /**
     * Returns the download directory for an anime if it exists.
     *
     * @param animeTitle the title of the anime to query.
     * @param source the source of the anime.
     */
    fun findAnimeDir(animeTitle: String, source: AnimeSource): UniFile? {
        val sourceDir = findSourceDir(source)
        return sourceDir?.findFile(getAnimeDirName(animeTitle))
    }

    /**
     * Returns the download directory for an episode if it exists.
     *
     * @param episodeName the name of the episode to query.
     * @param episodeScanlator scanlator of the episode to query
     * @param animeTitle the title of the anime to query.
     * @param source the source of the episode.
     */
    fun findEpisodeDir(
        episodeName: String,
        episodeScanlator: String?,
        animeTitle: String,
        source: AnimeSource,
    ): UniFile? {
        val animeDir = findAnimeDir(animeTitle, source)
        return getValidEpisodeDirNames(episodeName, episodeScanlator).asSequence()
            .mapNotNull { animeDir?.findFile(it) }
            .firstOrNull()
    }

    /**
     * Returns a list of downloaded directories for the episodes that exist.
     *
     * @param episodes the episodes to query.
     * @param anime the anime of the episode.
     * @param source the source of the episode.
     */
    fun findEpisodeDirs(episodes: List<Episode>, anime: Anime, source: AnimeSource): Pair<UniFile?, List<UniFile>> {
        // AM (CUSTOM_INFORMATION) -->
        val animeDir = findAnimeDir(anime.ogTitle, source) ?: return null to emptyList()
        // <-- AM (CUSTOM_INFORMATION)
        return animeDir to episodes.mapNotNull { episode ->
            getValidEpisodeDirNames(episode.name, episode.scanlator).asSequence()
                .mapNotNull { animeDir.findFile(it) }
                .firstOrNull()
        }
    }

    /**
     * Returns the download directory name for a source.
     *
     * @param source the source to query.
     */
    fun getSourceDirName(source: AnimeSource): String {
        return DiskUtil.buildValidFilename(source.toString())
    }

    /**
     * Returns the download directory name for an anime.
     *
     * @param animeTitle the title of the anime to query.
     */
    fun getAnimeDirName(animeTitle: String): String {
        return DiskUtil.buildValidFilename(animeTitle)
    }

    /**
     * Returns the episode directory name for an episode.
     *
     * @param episodeName the name of the episode to query.
     * @param episodeScanlator scanlator of the episode to query
     */
    fun getEpisodeDirName(episodeName: String, episodeScanlator: String?): String {
        val newEpisodeName = sanitizeEpisodeName(episodeName)
        return DiskUtil.buildValidFilename(
            when {
                !episodeScanlator.isNullOrBlank() -> "${episodeScanlator}_$newEpisodeName"
                else -> newEpisodeName
            },
        )
    }

    /**
     * Return the new name for the episode (in case it's empty or blank)
     *
     * @param episodeName the name of the episode
     */
    private fun sanitizeEpisodeName(episodeName: String): String {
        return episodeName.ifBlank {
            "Episode"
        }
    }

    // AY -->
    /**
     * Returns the episode directory name for an episode.
     *
     * @param episodeName the name of the episode to query.
     * @param episodeScanlator scanlator of the episode to query
     */
    fun getOldEpisodeDirName(episodeName: String, episodeScanlator: String?): String {
        return DiskUtil.buildValidFilename(
            when {
                episodeScanlator != null -> "${episodeScanlator}_$episodeName"
                else -> episodeName
            },
        )
    }
    // <-- AY

    fun isEpisodeDirNameChanged(oldEpisode: Episode, newEpisode: Episode): Boolean {
        return oldEpisode.name != newEpisode.name ||
            oldEpisode.scanlator?.takeIf { it.isNotBlank() } != newEpisode.scanlator?.takeIf { it.isNotBlank() }
    }

    /**
     * Returns valid downloaded episode directory names.
     *
     * @param episodeName the name of the episode to query.
     * @param episodeScanlator scanlator of the episode to query
     */
    fun getValidEpisodeDirNames(episodeName: String, episodeScanlator: String?): List<String> {
        val episodeDirName = getEpisodeDirName(episodeName, episodeScanlator)
        // AY -->
        val oldEpisodeDirName = getOldEpisodeDirName(episodeName, episodeScanlator)
        return listOf(episodeDirName, oldEpisodeDirName)
        // <-- AY
    }

    // AM (FILE_SIZE) -->

    /**
     * Returns an episode file size in bytes.
     * Returns null if the episode is not found in expected location
     *
     * @param episodeName the name of the episode to query.
     * @param episodeScanlator scanlator of the episode to query
     * @param animeTitle the title of the anime
     * @param animeSource the source of the anime
     */
    fun getEpisodeFileSize(
        episodeName: String,
        episodeUrl: String?,
        episodeScanlator: String?,
        animeTitle: String,
        animeSource: AnimeSource?,
    ): Long? {
        if (animeSource == null) return null
        return if (animeSource.isLocal()) {
            val (animeDirName, episodeDirName) = episodeUrl?.split('/', limit = 2) ?: return null
            localFileSystem.getBaseDirectory()?.findFile(animeDirName)?.findFile(episodeDirName)?.size()
        } else {
            findEpisodeDir(episodeName, episodeScanlator, animeTitle, animeSource)?.size()
        }
    }
    // <-- AM (FILE_SIZE)
}
