package eu.kanade.domain.chapter.interactor

import eu.kanade.domain.chapter.model.copyFromSChapter
import eu.kanade.domain.chapter.model.toSChapter
import eu.kanade.domain.manga.interactor.GetExcludedScanlators
import eu.kanade.domain.manga.interactor.UpdateManga
import eu.kanade.domain.manga.model.toSManga
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.download.DownloadProvider
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import tachiyomi.data.episode.EpisodeSanitizer
import tachiyomi.domain.episode.interactor.GetEpisodesByAnimeId
import tachiyomi.domain.episode.interactor.ShouldUpdateDbEpisode
import tachiyomi.domain.episode.interactor.UpdateEpisode
import tachiyomi.domain.episode.model.Episode
import tachiyomi.domain.episode.model.NoEpisodesException
import tachiyomi.domain.episode.model.toEpisodeUpdate
import tachiyomi.domain.episode.repository.EpisodeRepository
import tachiyomi.domain.episode.service.EpisodeRecognition
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.anime.model.Anime
import tachiyomi.source.local.isLocal
import java.lang.Long.max
import java.time.ZonedDateTime
import java.util.TreeSet

class SyncChaptersWithSource(
    private val downloadManager: DownloadManager,
    private val downloadProvider: DownloadProvider,
    private val episodeRepository: EpisodeRepository,
    private val shouldUpdateDbEpisode: ShouldUpdateDbEpisode,
    private val updateManga: UpdateManga,
    private val updateEpisode: UpdateEpisode,
    private val getEpisodesByAnimeId: GetEpisodesByAnimeId,
    private val getExcludedScanlators: GetExcludedScanlators,
    private val libraryPreferences: LibraryPreferences,
) {

    /**
     * Method to synchronize db chapters with source ones
     *
     * @param rawSourceChapters the chapters from the source.
     * @param anime the manga the chapters belong to.
     * @param source the source the manga belongs to.
     * @return Newly added chapters
     */
    suspend fun await(
        rawSourceChapters: List<SEpisode>,
        anime: Anime,
        source: AnimeSource,
        manualFetch: Boolean = false,
        fetchWindow: Pair<Long, Long> = Pair(0, 0),
    ): List<Episode> {
        if (rawSourceChapters.isEmpty() && !source.isLocal()) {
            throw NoEpisodesException()
        }

        val now = ZonedDateTime.now()
        val nowMillis = now.toInstant().toEpochMilli()

        val sourceEpisodes = rawSourceChapters
            .distinctBy { it.url }
            .mapIndexed { i, sChapter ->
                Episode.create()
                    .copyFromSChapter(sChapter)
                    .copy(name = with(EpisodeSanitizer) { sChapter.name.sanitize(anime.title) })
                    .copy(animeId = anime.id, sourceOrder = i.toLong())
            }

        val dbChapters = getEpisodesByAnimeId.await(anime.id)

        val newEpisodes = mutableListOf<Episode>()
        val updatedEpisodes = mutableListOf<Episode>()
        val removedChapters = dbChapters.filterNot { dbChapter ->
            sourceEpisodes.any { sourceChapter ->
                dbChapter.url == sourceChapter.url
            }
        }

        // Used to not set upload date of older chapters
        // to a higher value than newer chapters
        var maxSeenUploadDate = 0L

        for (sourceChapter in sourceEpisodes) {
            var chapter = sourceChapter

            // Update metadata from source if necessary.
            if (source is AnimeHttpSource) {
                val sChapter = chapter.toSChapter()
                source.prepareNewEpisode(sChapter, anime.toSManga())
                chapter = chapter.copyFromSChapter(sChapter)
            }

            // Recognize chapter number for the chapter.
            val chapterNumber = EpisodeRecognition.parseEpisodeNumber(anime.title, chapter.name, chapter.episodeNumber)
            chapter = chapter.copy(episodeNumber = chapterNumber)

            val dbChapter = dbChapters.find { it.url == chapter.url }

            if (dbChapter == null) {
                val toAddChapter = if (chapter.dateUpload == 0L) {
                    val altDateUpload = if (maxSeenUploadDate == 0L) nowMillis else maxSeenUploadDate
                    chapter.copy(dateUpload = altDateUpload)
                } else {
                    maxSeenUploadDate = max(maxSeenUploadDate, sourceChapter.dateUpload)
                    chapter
                }
                newEpisodes.add(toAddChapter)
            } else {
                if (shouldUpdateDbEpisode.await(dbChapter, chapter)) {
                    val shouldRenameChapter = downloadProvider.isChapterDirNameChanged(dbChapter, chapter) &&
                        downloadManager.isChapterDownloaded(
                            dbChapter.name,
                            dbChapter.scanlator,
                            anime.title,
                            anime.source,
                        )

                    if (shouldRenameChapter) {
                        downloadManager.renameChapter(source, anime, dbChapter, chapter)
                    }
                    var toChangeChapter = dbChapter.copy(
                        name = chapter.name,
                        episodeNumber = chapter.episodeNumber,
                        scanlator = chapter.scanlator,
                        sourceOrder = chapter.sourceOrder,
                    )
                    if (chapter.dateUpload != 0L) {
                        toChangeChapter = toChangeChapter.copy(dateUpload = chapter.dateUpload)
                    }
                    updatedEpisodes.add(toChangeChapter)
                }
            }
        }

        // Return if there's nothing to add, delete, or update to avoid unnecessary db transactions.
        if (newEpisodes.isEmpty() && removedChapters.isEmpty() && updatedEpisodes.isEmpty()) {
            if (manualFetch || anime.fetchInterval == 0 || anime.nextUpdate < fetchWindow.first) {
                updateManga.awaitUpdateFetchInterval(
                    anime,
                    now,
                    fetchWindow,
                )
            }
            return emptyList()
        }

        val changedOrDuplicateReadUrls = mutableSetOf<String>()

        val deletedChapterNumbers = TreeSet<Double>()
        val deletedReadChapterNumbers = TreeSet<Double>()
        val deletedBookmarkedChapterNumbers = TreeSet<Double>()

        val readChapterNumbers = dbChapters
            .asSequence()
            .filter { it.seen && it.isRecognizedNumber }
            .map { it.episodeNumber }
            .toSet()

        removedChapters.forEach { chapter ->
            if (chapter.seen) deletedReadChapterNumbers.add(chapter.episodeNumber)
            if (chapter.bookmark) deletedBookmarkedChapterNumbers.add(chapter.episodeNumber)
            deletedChapterNumbers.add(chapter.episodeNumber)
        }

        val deletedChapterNumberDateFetchMap = removedChapters.sortedByDescending { it.dateFetch }
            .associate { it.episodeNumber to it.dateFetch }

        val markDuplicateAsRead = libraryPreferences.markDuplicateSeenEpisodeAsSeen().get()
            .contains(LibraryPreferences.MARK_DUPLICATE_EPISODE_SEEN_NEW)

        // Date fetch is set in such a way that the upper ones will have bigger value than the lower ones
        // Sources MUST return the chapters from most to less recent, which is common.
        var itemCount = newEpisodes.size
        var updatedToAdd = newEpisodes.map { toAddItem ->
            var chapter = toAddItem.copy(dateFetch = nowMillis + itemCount--)

            if (chapter.episodeNumber in readChapterNumbers && markDuplicateAsRead) {
                changedOrDuplicateReadUrls.add(chapter.url)
                chapter = chapter.copy(seen = true)
            }

            if (!chapter.isRecognizedNumber || chapter.episodeNumber !in deletedChapterNumbers) return@map chapter

            chapter = chapter.copy(
                seen = chapter.episodeNumber in deletedReadChapterNumbers,
                bookmark = chapter.episodeNumber in deletedBookmarkedChapterNumbers,
            )

            // Try to to use the fetch date of the original entry to not pollute 'Updates' tab
            deletedChapterNumberDateFetchMap[chapter.episodeNumber]?.let {
                chapter = chapter.copy(dateFetch = it)
            }

            changedOrDuplicateReadUrls.add(chapter.url)

            chapter
        }

        if (removedChapters.isNotEmpty()) {
            val toDeleteIds = removedChapters.map { it.id }
            episodeRepository.removeEpisodesWithIds(toDeleteIds)
        }

        if (updatedToAdd.isNotEmpty()) {
            updatedToAdd = episodeRepository.addAll(updatedToAdd)
        }

        if (updatedEpisodes.isNotEmpty()) {
            val chapterUpdates = updatedEpisodes.map { it.toEpisodeUpdate() }
            updateEpisode.awaitAll(chapterUpdates)
        }
        updateManga.awaitUpdateFetchInterval(anime, now, fetchWindow)

        // Set this manga as updated since chapters were changed
        // Note that last_update actually represents last time the chapter list changed at all
        updateManga.awaitUpdateLastUpdate(anime.id)

        val excludedScanlators = getExcludedScanlators.await(anime.id).toHashSet()

        return updatedToAdd.filterNot { it.url in changedOrDuplicateReadUrls || it.scanlator in excludedScanlators }
    }
}
