package eu.kanade.domain.manga.interactor

import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import tachiyomi.domain.anime.model.AnimeUpdate
import tachiyomi.domain.anime.repository.AnimeRepository

class SetMangaViewerFlags(
    private val animeRepository: AnimeRepository,
) {

    suspend fun awaitSetReadingMode(id: Long, flag: Long) {
        val manga = animeRepository.getAnimeById(id)
        animeRepository.update(
            AnimeUpdate(
                id = id,
                viewerFlags = manga.viewerFlags.setFlag(flag, ReadingMode.MASK.toLong()),
            ),
        )
    }

    suspend fun awaitSetOrientation(id: Long, flag: Long) {
        val manga = animeRepository.getAnimeById(id)
        animeRepository.update(
            AnimeUpdate(
                id = id,
                viewerFlags = manga.viewerFlags.setFlag(flag, ReaderOrientation.MASK.toLong()),
            ),
        )
    }

    private fun Long.setFlag(flag: Long, mask: Long): Long {
        return this and mask.inv() or (flag and mask)
    }
}
