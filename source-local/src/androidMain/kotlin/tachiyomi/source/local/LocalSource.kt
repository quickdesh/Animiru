package tachiyomi.source.local

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.UnmeteredSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.util.lang.compareToCaseInsensitiveNaturalOrder
import eu.kanade.tachiyomi.util.storage.toFFmpegString
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import logcat.LogPriority
import rx.Observable
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.storage.extension
import tachiyomi.core.common.storage.nameWithoutExtension
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.core.metadata.tachiyomi.AnimeDetails
import tachiyomi.core.metadata.tachiyomi.EpisodeDetails
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.episode.service.EpisodeRecognition
import tachiyomi.i18n.MR
import tachiyomi.source.local.filter.OrderBy
import tachiyomi.source.local.image.LocalCoverManager
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
) : AnimeCatalogueSource, UnmeteredSource {

    private val json: Json by injectLazy()

    @Suppress("PrivatePropertyName")
    private val PopularFilters = AnimeFilterList(OrderBy.Popular(context))

    @Suppress("PrivatePropertyName")
    private val LatestFilters = AnimeFilterList(OrderBy.Latest(context))

    override val name: String = context.stringResource(MR.strings.local_source)

    override val id: Long = ID

    override val lang: String = "other"

    override fun toString() = name

    override val supportsLatest: Boolean = true

    // Browse related
    override suspend fun getPopularAnime(page: Int) = getSearchAnime(page, "", PopularFilters)

    override suspend fun getLatestUpdates(page: Int) = getSearchAnime(page, "", LatestFilters)

    override suspend fun getSearchAnime(
        page: Int,
        query: String,
        filters: AnimeFilterList,
    ): AnimesPage = withIOContext {
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
                    SAnime.create().apply {
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

        AnimesPage(animes, false)
    }

    // Old fetch functions

    // TODO: Should be replaced when Anime Extensions get to 1.15

    @Deprecated("Use the non-RxJava API instead", replaceWith = ReplaceWith("getPopularAnime"))
    override fun fetchPopularAnime(page: Int) = fetchSearchAnime(page, "", PopularFilters)

    @Deprecated("Use the non-RxJava API instead", replaceWith = ReplaceWith("getLatestUpdates"))
    override fun fetchLatestUpdates(page: Int) = fetchSearchAnime(page, "", LatestFilters)

    @Deprecated("Use the non-RxJava API instead", replaceWith = ReplaceWith("getSearchAnime"))
    override fun fetchSearchAnime(page: Int, query: String, filters: AnimeFilterList): Observable<AnimesPage> {
        return runBlocking {
            Observable.just(getSearchAnime(page, query, filters))
        }
    }

    // AM (CUSTOM_INFORMATION) -->
    fun updateAnimeInfo(anime: SAnime) {
        val directory = fileSystem.getAnimeDirectory(anime.url) ?: return
        val existingFileName = directory.listFiles()?.find {
            it.extension == "json" && it.nameWithoutExtension == "details"
        }?.name
        val file = directory.createFile(existingFileName ?: "info.json") ?: return
        file.openOutputStream().use {
            json.encodeToStream(anime.toJson(), it)
        }
    }

    private fun SAnime.toJson(): AnimeDetails {
        return AnimeDetails(title, author, artist, description, genre?.split(", "), status)
    }
    // <-- AM (CUSTOM_INFORMATION)

    // Anime details related
    override suspend fun getAnimeDetails(anime: SAnime): SAnime = withIOContext {
        coverManager.find(anime.url)?.let {
            anime.thumbnail_url = it.uri.toString()
        }

        // Augment anime details based on metadata files
        try {
            val animeDirFiles = fileSystem.getFilesInAnimeDirectory(anime.url)

            animeDirFiles
                .firstOrNull { it.extension == "json" && it.nameWithoutExtension == "details" }
                ?.let { file ->
                    json.decodeFromStream<AnimeDetails>(file.openInputStream()).run {
                        title?.let { anime.title = it }
                        author?.let { anime.author = it }
                        artist?.let { anime.artist = it }
                        description?.let { anime.description = it }
                        genre?.let { anime.genre = it.joinToString() }
                        status?.let { anime.status = it }
                    }
                }
        } catch (e: Throwable) {
            logcat(LogPriority.ERROR, e) { "Error setting anime details from local metadata for ${anime.title}" }
        }

        return@withIOContext anime
    }

    // Episodes
    override suspend fun getEpisodeList(anime: SAnime): List<SEpisode> = withIOContext {
        // AY -->
        val episodesData = fileSystem.getFilesInAnimeDirectory(anime.url)
            .firstOrNull {
                it.extension == "json" && it.nameWithoutExtension == "episodes"
            }?.let { file ->
                runCatching {
                    json.decodeFromStream<List<EpisodeDetails>>(file.openInputStream())
                }.getOrNull()
            }
        // <-- AY

        val episodes = fileSystem.getFilesInAnimeDirectory(anime.url)
            // Only keep supported formats
            .filterNot { it.name.orEmpty().startsWith('.') }
            .filter { Format.isSupported(it) }
            .map { episodeFile ->
                SEpisode.create().apply {
                    url = "${anime.url}/${episodeFile.name}"
                    name = episodeFile.nameWithoutExtension.orEmpty()
                    date_upload = episodeFile.lastModified()

                    val episodeNumber = EpisodeRecognition
                        .parseEpisodeNumber(anime.title, this.name, this.episode_number.toDouble())
                        .toFloat()
                    episode_number = episodeNumber

                    // AY -->
                    // Overwrite data from episodes.json file
                    episodesData?.also { dataList ->
                        dataList.firstOrNull { it.episodeNumber.equalsTo(episodeNumber) }?.also { data ->
                            data.name?.also { name = it }
                            data.dateUpload?.also { date_upload = parseDate(it) }
                            scanlator = data.scanlator
                        }
                    }
                    // <-- AY
                }
            }
            .sortedWith { e1, e2 ->
                e2.name.compareToCaseInsensitiveNaturalOrder(e1.name)
            }

        // Generate the cover from the first episode found if not available
        if (anime.thumbnail_url.isNullOrBlank()) {
            try {
                episodes.lastOrNull()?.let { episode ->
                    updateCover(episode, anime)
                }
            } catch (e: Exception) {
                logcat(LogPriority.ERROR) { "Couldn't extract thumbnail from video: $e" }
            }
        }

        episodes
    }

    // AY -->
    private fun parseDate(isoDate: String): Long {
        return dateFormat.parse(isoDate)?.time ?: 0L
    }

    private fun Float.equalsTo(other: Float): Boolean {
        return abs(this - other) < 0.0001
    }
    // <-- AY

    // Filters
    override fun getFilterList() = AnimeFilterList(OrderBy.Popular(context))

    // Unused stuff
    override suspend fun getVideoList(episode: SEpisode): List<Video> = throw UnsupportedOperationException("Unused")

    // AY -->
    private fun updateCover(episode: SEpisode, anime: SAnime) {
        val tempFile = File.createTempFile(
            "tmp_",
            anime.title + DEFAULT_COVER_NAME,
        )
        val outFile = tempFile.path

        val episodeName = episode.url.split('/', limit = 2).last()
        val animeDir = fileSystem.getAnimeDirectory(anime.url)!!
        val episodeFile = animeDir.findFile(episodeName)!!
        val episodeFilename = { episodeFile.toFFmpegString(context) }

        val ffProbe = FFprobeKit.execute(
            "-v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"${episodeFilename()}\"",
        )
        val duration = ffProbe.allLogsAsString.trim().toFloat()
        val second = duration.toInt() / 2

        FFmpegKit.execute(
            "-ss $second -i \"${episodeFilename()}\" -frames:v 1 -update true \"$outFile\" -y",
        )

        if (tempFile.length() > 0L) {
            coverManager.update(anime, tempFile.inputStream())
        }
    }
    // <-- AY

    companion object {
        const val ID = 0L
        const val HELP_URL = "https://aniyomi.org/help/guides/local-anime/"

        // AY -->
        private val dateFormat by lazy { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) }
        private const val DEFAULT_COVER_NAME = "cover.jpg"
        // <-- AY
        private val LATEST_THRESHOLD = 7.days.inWholeMilliseconds
    }
}

fun Anime.isLocal(): Boolean = source == LocalSource.ID

fun AnimeSource.isLocal(): Boolean = id == LocalSource.ID

fun DomainSource.isLocal(): Boolean = id == LocalSource.ID
