package tachiyomi.source.local

import android.content.Context
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.UnmeteredSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.lang.compareToCaseInsensitiveNaturalOrder
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import logcat.LogPriority
import mihon.core.archive.archiveReader
import mihon.core.archive.epubReader
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.storage.extension
import tachiyomi.core.common.storage.nameWithoutExtension
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.system.ImageUtil
import tachiyomi.core.common.util.system.logcat
import tachiyomi.core.metadata.tachiyomi.EpisodeDetails
import tachiyomi.core.metadata.tachiyomi.MangaDetails
import tachiyomi.domain.chapter.service.ChapterRecognition
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.source.local.filter.OrderBy
import tachiyomi.source.local.image.LocalCoverManager
import tachiyomi.source.local.io.Formats
import tachiyomi.source.local.io.Format
import tachiyomi.source.local.io.LocalSourceFileSystem
import uy.kohesive.injekt.injectLazy
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.time.Duration.Companion.days
import tachiyomi.domain.source.model.Source as DomainSource

actual class LocalSource(
    private val context: Context,
    private val fileSystem: LocalSourceFileSystem,
    private val coverManager: LocalCoverManager,
) : CatalogueSource, UnmeteredSource {

    private val json: Json by injectLazy()

    @Suppress("PrivatePropertyName")
    private val PopularFilters = FilterList(OrderBy.Popular(context))

    @Suppress("PrivatePropertyName")
    private val LatestFilters = FilterList(OrderBy.Latest(context))

    override val name: String = context.stringResource(MR.strings.local_source)

    override val id: Long = ID

    override val lang: String = "other"

    override fun toString() = name

    override val supportsLatest: Boolean = true

    // Browse related
    override suspend fun getPopularManga(page: Int) = getSearchManga(page, "", PopularFilters)

    override suspend fun getLatestUpdates(page: Int) = getSearchManga(page, "", LatestFilters)

    override suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage = withIOContext {
        val lastModifiedLimit = if (filters === LatestFilters) {
            System.currentTimeMillis() - LATEST_THRESHOLD
        } else {
            0L
        }

        var animeDirs = fileSystem.getFilesInBaseDirectory()
            // Filter out files that are hidden and is not a folder
            .filter { it.isDirectory && !it.name.orEmpty().startsWith('.') }
            .distinctBy { it.name }
            .filter {
                if (lastModifiedLimit == 0L && query.isBlank()) {
                    true
                } else if (lastModifiedLimit == 0L) {
                    it.name.orEmpty().contains(query, ignoreCase = true)
                } else {
                    it.lastModified() >= lastModifiedLimit
                }
            }

        filters.forEach { filter ->
            when (filter) {
                is OrderBy.Popular -> {
                    animeDirs = if (filter.state!!.ascending) {
                        animeDirs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name.orEmpty() })
                    } else {
                        animeDirs.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name.orEmpty() })
                    }
                }
                is OrderBy.Latest -> {
                    animeDirs = if (filter.state!!.ascending) {
                        animeDirs.sortedBy(UniFile::lastModified)
                    } else {
                        animeDirs.sortedByDescending(UniFile::lastModified)
                    }
                }
                else -> {
                    /* Do nothing */
                }
            }
        }

        val animes = animeDirs
            .map { animeDir ->
                async {
                    SManga.create().apply {
                        title = animeDir.name.orEmpty()
                        url = animeDir.name.orEmpty()

                        // Try to find the cover
                        coverManager.find(animeDir.name.orEmpty())?.let {
                            thumbnail_url = it.uri.toString()
                        }
                    }
                }
            }
            .awaitAll()

        MangasPage(animes, false)
    }

    // AM (CUSTOM_INFORMATION) -->
    fun updateAnimeInfo(anime: SManga) {
        val directory = fileSystem.getAnimeDirectory(anime.url) ?: return
        val existingFileName = directory.listFiles()?.find {
            it.extension == "json" && it.nameWithoutExtension == "details"
        }?.name
        val file = directory.createFile(existingFileName ?: "info.json") ?: return
        file.openOutputStream().use {
            json.encodeToStream(anime.toJson(), it)
        }
    }

    private fun SManga.toJson(): MangaDetails {
        return MangaDetails(title, author, artist, description, genre?.split(", "), status)
    }
    // <-- AM (CUSTOM_INFORMATION)

    // Anime details related
    override suspend fun getMangaDetails(manga: SManga): SManga = withIOContext {
        coverManager.find(manga.url)?.let {
            manga.thumbnail_url = it.uri.toString()
        }

        // Augment anime details based on metadata files
        try {
            val animeDirFiles = fileSystem.getFilesInAnimeDirectory(manga.url)

            animeDirFiles
                .firstOrNull { it.extension == "json" && it.nameWithoutExtension == "details" }
                ?.let { file ->
                    json.decodeFromStream<MangaDetails>(file.openInputStream()).run {
                        title?.let { manga.title = it }
                        author?.let { manga.author = it }
                        artist?.let { manga.artist = it }
                        description?.let { manga.description = it }
                        genre?.let { manga.genre = it.joinToString() }
                        status?.let { manga.status = it }
                    }
                }
        } catch (e: Throwable) {
            logcat(LogPriority.ERROR, e) { "Error setting anime details from local metadata for ${manga.title}" }
        }

        return@withIOContext manga
    }

    // Episodes
    override suspend fun getChapterList(manga: SManga): List<SChapter> = withIOContext {
        val episodesData = fileSystem.getFilesInAnimeDirectory(manga.url)
            .firstOrNull {
                it.extension == "json" && it.nameWithoutExtension == "episodes"
            }?.let { file ->
                runCatching {
                    json.decodeFromStream<List<EpisodeDetails>>(file.openInputStream())
                }.getOrNull()
            }

        val episodes = fileSystem.getFilesInAnimeDirectory(manga.url)
            // Only keep supported formats
            .filterNot { it.name.orEmpty().startsWith('.') }
            .filter { Formats.isSupported(it) }
            .map { episodeFile ->
                SChapter.create().apply {
                    url = "${manga.url}/${episodeFile.name}"
                    name = episodeFile.nameWithoutExtension.orEmpty()
                    date_upload = episodeFile.lastModified()

                    val episodeNumber = ChapterRecognition
                        .parseChapterNumber(manga.title, this.name, this.chapter_number.toDouble())
                        .toFloat()
                    chapter_number = episodeNumber

                    // Overwrite data from episodes.json file
                    episodesData?.also { dataList ->
                        dataList.firstOrNull { it.episodeNumber.equalsTo(episodeNumber) }?.also { data ->
                            data.name?.also { name = it }
                            data.dateUpload?.also { date_upload = parseDate(it) }
                            scanlator = data.scanlator
                        }
                    }
                }
            }
            .sortedWith { e1, e2 ->
                val c = e2.chapter_number.compareTo(e1.chapter_number)
                if (c == 0) e2.name.compareToCaseInsensitiveNaturalOrder(e1.name) else c
            }

        // Generate the cover from the first episode found if not available
        if (manga.thumbnail_url.isNullOrBlank()) {
            try {
                episodes.lastOrNull()?.let { episode ->
                    updateCover(episode, manga)
                }
            } catch (e: Exception) {
                logcat(LogPriority.ERROR) { "Couldn't extract thumbnail from video: $e" }
            }
        }

        episodes
    }

    private fun parseDate(isoDate: String): Long {
        return dateFormat.parse(isoDate)?.time ?: 0L
    }

    private fun Float.equalsTo(other: Float): Boolean {
        return abs(this - other) < 0.0001
    }

    // Filters
    override fun getFilterList() = FilterList(OrderBy.Popular(context))

    // Unused stuff
    override suspend fun getPageList(chapter: SChapter): List<Page> = throw UnsupportedOperationException("Unused")

    private fun updateCover(episode: SChapter, anime: SManga) {
        val tempFile = File.createTempFile(
            "tmp_",
            anime.title + DEFAULT_COVER_NAME,
        )
        val outFile = tempFile.path

        val episodeName = episode.url.split('/', limit = 2).last()
        val animeDir = fileSystem.getAnimeDirectory(anime.url)!!
        val episodeFile = animeDir.findFile(episodeName)!!
        val episodeFilename = { episodeFile.toFFmpegString(context) }

        val ffProbe = com.arthenica.ffmpegkit.FFprobeKit.execute(
            "-v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"${episodeFilename()}\"",
        )
        val duration = ffProbe.allLogsAsString.trim().toFloat()
        val second = duration.toInt() / 2

        com.arthenica.ffmpegkit.FFmpegKit.execute(
            "-ss $second -i \"${episodeFilename()}\" -frames:v 1 -update true \"$outFile\" -y",
        )

        if (tempFile.length() > 0L) {
            coverManager.update(anime, tempFile.inputStream())
        }
    }

    companion object {
        const val ID = 0L
        const val HELP_URL = "https://aniyomi.org/help/guides/local-anime/"

        private val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }
        private const val DEFAULT_COVER_NAME = "cover.jpg"
        private val LATEST_THRESHOLD = 7.days.inWholeMilliseconds
    }
}

fun Manga.isLocal(): Boolean = source == LocalSource.ID

fun Source.isLocal(): Boolean = id == LocalSource.ID

fun DomainSource.isLocal(): Boolean = id == LocalSource.ID
