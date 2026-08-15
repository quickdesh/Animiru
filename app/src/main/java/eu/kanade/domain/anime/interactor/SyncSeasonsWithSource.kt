// AY -->
package eu.kanade.domain.anime.interactor

import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.SAnime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mihon.domain.anime.model.toDomainAnime
import tachiyomi.domain.anime.interactor.NetworkToLocalAnime
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.model.NoSeasonsException
import tachiyomi.domain.anime.model.toAnimeUpdate
import tachiyomi.domain.anime.repository.AnimeRepository
import tachiyomi.domain.season.interactor.GetAnimeSeasonsByParentId
import tachiyomi.domain.season.interactor.ShouldUpdateDbSeason
import tachiyomi.domain.season.service.SeasonRecognition
import tachiyomi.source.local.isLocal
import kotlin.time.Clock

class SyncSeasonsWithSource(
    private val updateAnime: UpdateAnime,
    private val animeRepository: AnimeRepository,
    private val networkToLocalAnime: NetworkToLocalAnime,
    private val shouldUpdateDbSeason: ShouldUpdateDbSeason,
    private val getAnimeSeasonsByParentId: GetAnimeSeasonsByParentId,
) {
    suspend fun await(
        rawSourceSeasons: List<SAnime>,
        anime: Anime,
        source: AnimeSource,
        manualFetch: Boolean = false,
        fetchWindow: Pair<Long, Long> = Pair(0, 0),
    ): List<Anime> {
        if (rawSourceSeasons.isEmpty() && !source.isLocal()) {
            throw NoSeasonsException()
        }

        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(timeZone)

        val sourceSeasons = rawSourceSeasons
            .distinctBy { it.url }
            .mapIndexed { i, sAnime ->
                networkToLocalAnime.invoke(sAnime.toDomainAnime(source.id))
                    .copy(parentId = anime.id, seasonSourceOrder = i.toLong())
            }

        val dbSeasons = getAnimeSeasonsByParentId.await(anime.id)

        val newSeasons = mutableListOf<Anime>()
        val updatedSeasons = mutableListOf<Anime>()
        val removedSeasons = dbSeasons.filterNot { dbSeasons ->
            sourceSeasons.any { sourceSeason ->
                dbSeasons.anime.url == sourceSeason.url
            }
        }

        for (sourceSeason in sourceSeasons) {
            var season = sourceSeason

            // Recognize season number for the season
            val seasonNumber = SeasonRecognition.parseSeasonNumber(
                anime.title,
                season.title,
                season.seasonNumber,
            )
            season = season.copy(seasonNumber = seasonNumber)

            val dbSeason = dbSeasons.find { it.anime.url == season.url }?.anime
            if (dbSeason == null) {
                newSeasons.add(season)
            } else {
                if (shouldUpdateDbSeason.await(dbSeason, season)) {
                    val toChangeSeason = dbSeason.copy(
                        // AM (CUSTOM_INFORMATION) -->
                        ogTitle = season.title,
                        // <-- AM (CUSTOM_INFORMATION)
                        seasonNumber = season.seasonNumber,
                        seasonSourceOrder = season.seasonSourceOrder,
                        memo = season.memo,
                    )
                    updatedSeasons.add(toChangeSeason)
                }
            }
        }

        // Return if there's nothing to add, delete, or update to avoid unnecessary db transactions.
        if (newSeasons.isEmpty() && removedSeasons.isEmpty() && updatedSeasons.isEmpty()) {
            if (manualFetch || anime.fetchInterval == 0 || anime.nextUpdate < fetchWindow.first) {
                updateAnime.awaitUpdateFetchInterval(
                    anime,
                    timeZone,
                    now,
                    fetchWindow,
                )
            }
            return sourceSeasons
        }

        if (removedSeasons.isNotEmpty()) {
            val toDeleteIds = removedSeasons.map { it.id }
            animeRepository.removeParentIdByIds(toDeleteIds)
        }

        val toUpdate = newSeasons.map { it.toAnimeUpdate() } +
            updatedSeasons.map { it.toAnimeUpdate() }

        if (toUpdate.isNotEmpty()) {
            updateAnime.awaitAll(toUpdate)
        }

        updateAnime.awaitUpdateLastUpdate(anime.id)

        return sourceSeasons
    }
}
// <-- AY
