package mihon.feature.migration.list

import androidx.annotation.FloatRange
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import eu.kanade.domain.episode.interactor.SyncEpisodesWithSource
import eu.kanade.domain.anime.interactor.UpdateAnime
import eu.kanade.domain.anime.model.toSAnime
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.source.getNameForAnimeInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import logcat.LogPriority
import mihon.domain.migration.usecases.MigrateAnimeUseCase
import mihon.feature.migration.list.models.MigratingAnime
import mihon.feature.migration.list.models.MigratingAnime.SearchResult
import mihon.feature.migration.list.search.SmartSourceSearchEngine
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.core.common.util.lang.withUIContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.episode.interactor.GetEpisodesByAnimeId
import tachiyomi.domain.anime.interactor.GetAnime
import tachiyomi.domain.anime.interactor.NetworkToLocalAnime
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.source.service.SourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MigrationListScreenModel(
    animeIds: List<Long>,
    extraSearchQuery: String?,
    private val preferences: SourcePreferences = Injekt.get(),
    private val sourceManager: SourceManager = Injekt.get(),
    private val getAnime: GetAnime = Injekt.get(),
    private val networkToLocalAnime: NetworkToLocalAnime = Injekt.get(),
    private val updateAnime: UpdateAnime = Injekt.get(),
    private val syncEpisodesWithSource: SyncEpisodesWithSource = Injekt.get(),
    private val getEpisodesByAnimeId: GetEpisodesByAnimeId = Injekt.get(),
    private val migrateAnime: MigrateAnimeUseCase = Injekt.get(),
) : StateScreenModel<MigrationListScreenModel.State>(State()) {

    private val smartSearchEngine = SmartSourceSearchEngine(extraSearchQuery)

    val items
        inline get() = state.value.items

    private val hideUnmatched = preferences.migrationHideUnmatched().get()
    private val hideWithoutUpdates = preferences.migrationHideWithoutUpdates().get()

    private val navigateBackChannel = Channel<Unit>()
    val navigateBackEvent = navigateBackChannel.receiveAsFlow()

    private var migrateJob: Job? = null

    init {
        screenModelScope.launchIO {
            val anime = animeIds
                .map {
                    async {
                        val anime = getAnime.await(it) ?: return@async null
                        val episodeInfo = getEpisodeInfo(it)
                        MigratingAnime(
                            anime = anime,
                            episodeCount = episodeInfo.episodeCount,
                            latestEpisode = episodeInfo.latestEpisode,
                            source = sourceManager.getOrStub(anime.source).getNameForAnimeInfo(),
                            parentContext = screenModelScope.coroutineContext,
                        )
                    }
                }
                .awaitAll()
                .filterNotNull()
            mutableState.update { it.copy(items = anime.toImmutableList()) }
            runMigrations(anime)
        }
    }

    private suspend fun getEpisodeInfo(id: Long) = getEpisodesByAnimeId.await(id).let { episodes ->
        EpisodeInfo(
            latestEpisode = episodes.maxOfOrNull { it.episodeNumber },
            episodeCount = episodes.size,
        )
    }

    private suspend fun Anime.toSuccessSearchResult(): SearchResult.Success {
        val episodeInfo = getEpisodeInfo(id)
        val source = sourceManager.getOrStub(source).getNameForAnimeInfo()
        return SearchResult.Success(
            anime = this,
            episodeCount = episodeInfo.episodeCount,
            latestEpisode = episodeInfo.latestEpisode,
            source = source,
        )
    }

    private suspend fun runMigrations(animes: List<MigratingAnime>) {
        val prioritizeByEpisodes = preferences.migrationPrioritizeByEpisodes().get()
        val deepSearchMode = preferences.migrationDeepSearchMode().get()

        val sources = preferences.migrationSources().get()
            .mapNotNull { sourceManager.get(it) as? AnimeCatalogueSource }

        for (anime in animes) {
            if (!currentCoroutineContext().isActive) break
            if (anime.anime.id !in state.value.animeIds) continue
            if (anime.searchResult.value != SearchResult.Searching) continue
            if (!anime.migrationScope.isActive) continue

            val result = try {
                anime.migrationScope.async {
                    if (prioritizeByEpisodes) {
                        val sourceSemaphore = Semaphore(5)
                        sources.map { source ->
                            async innerAsync@{
                                sourceSemaphore.withPermit {
                                    val result = searchSource(anime.anime, source, deepSearchMode)
                                    if (result == null || result.second.episodeCount == 0) return@innerAsync null
                                    result
                                }
                            }
                        }
                            .mapNotNull { it.await() }
                            .maxByOrNull { it.second.latestEpisode ?: 0.0 }
                    } else {
                        sources.forEach { source ->
                            val result = searchSource(anime.anime, source, deepSearchMode)
                            if (result != null) return@async result
                        }
                        null
                    }
                }
                    .await()
            } catch (e: CancellationException) {
                continue
            }

            if (result != null && result.first.thumbnailUrl == null) {
                try {
                    val newAnime = sourceManager.getOrStub(result.first.source).getAnimeDetails(result.first.toSAnime())
                    updateAnime.awaitUpdateFromSource(result.first, newAnime, true)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                }
            }

            anime.searchResult.value = result?.first?.toSuccessSearchResult() ?: SearchResult.NotFound

            if (result == null && hideUnmatched) {
                removeAnime(anime)
            }
            if (result != null &&
                hideWithoutUpdates &&
                (result.second.latestEpisode ?: 0.0) <= (anime.latestEpisode ?: 0.0)
            ) {
                removeAnime(anime)
            }

            updateMigrationProgress()
        }
    }

    private suspend fun searchSource(
        anime: Anime,
        source: AnimeCatalogueSource,
        deepSearchMode: Boolean,
    ): Pair<Anime, EpisodeInfo>? {
        return try {
            val searchResult = if (deepSearchMode) {
                smartSearchEngine.deepSearch(source, anime.title)
            } else {
                smartSearchEngine.regularSearch(source, anime.title)
            }

            if (searchResult == null || !(searchResult.url == anime.url && source.id == anime.source)) return null

            val localAnime = networkToLocalAnime(searchResult)
            try {
                val episodes = source.getEpisodeList(localAnime.toSAnime())
                syncEpisodesWithSource.await(episodes, localAnime, source)
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e)
            }
            localAnime to getEpisodeInfo(localAnime.id)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun updateMigrationProgress() {
        mutableState.update { state ->
            state.copy(
                finishedCount = items.count { it.searchResult.value != SearchResult.Searching },
                migrationComplete = migrationComplete(),
            )
        }
        if (items.isEmpty()) {
            navigateBack()
        }
    }

    private fun migrationComplete() = items.all { it.searchResult.value != SearchResult.Searching } &&
        items.any { it.searchResult.value is SearchResult.Success }

    fun useAnimeForMigration(current: Long, target: Long, onMissingEpisodes: () -> Unit) {
        val migratingAnime = items.find { it.anime.id == current } ?: return
        migratingAnime.searchResult.value = SearchResult.Searching
        screenModelScope.launchIO {
            val result = migratingAnime.migrationScope.async {
                val anime = getAnime.await(target) ?: return@async null
                try {
                    val source = sourceManager.get(anime.source)!!
                    val episodes = source.getEpisodeList(anime.toSAnime())
                    syncEpisodesWithSource.await(episodes, anime, source)
                } catch (_: Exception) {
                    return@async null
                }
                anime
            }
                .await()

            if (result == null) {
                migratingAnime.searchResult.value = SearchResult.NotFound
                withUIContext { onMissingEpisodes() }
                return@launchIO
            }

            try {
                val newAnime = sourceManager.getOrStub(result.source).getAnimeDetails(result.toSAnime())
                updateAnime.awaitUpdateFromSource(result, newAnime, true)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
            migratingAnime.searchResult.value = result.toSuccessSearchResult()
        }
    }

    fun migrateAnimes() {
        migrateAnimes(replace = true)
    }

    fun copyAnimes() {
        migrateAnimes(replace = false)
    }

    private fun migrateAnimes(replace: Boolean) {
        migrateJob = screenModelScope.launchIO {
            mutableState.update { it.copy(dialog = Dialog.Progress(0f)) }
            val items = items
            try {
                items.forEachIndexed { index, anime ->
                    try {
                        ensureActive()
                        val target = anime.searchResult.value.let {
                            if (it is SearchResult.Success) {
                                it.anime
                            } else {
                                null
                            }
                        }
                        if (target != null) {
                            migrateAnime(current = anime.anime, target = target, replace = replace)
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        logcat(LogPriority.WARN, throwable = e)
                    }
                    mutableState.update {
                        it.copy(dialog = Dialog.Progress((index.toFloat() / items.size).coerceAtMost(1f)))
                    }
                }

                navigateBack()
            } finally {
                mutableState.update { it.copy(dialog = null) }
                migrateJob = null
            }
        }
    }

    fun cancelMigrate() {
        migrateJob?.cancel()
        migrateJob = null
    }

    private suspend fun navigateBack() {
        navigateBackChannel.send(Unit)
    }

    fun migrateNow(animeId: Long, replace: Boolean) {
        screenModelScope.launchIO {
            val anime = items.find { it.anime.id == animeId } ?: return@launchIO
            val target = (anime.searchResult.value as? SearchResult.Success)?.anime ?: return@launchIO
            migrateAnime(current = anime.anime, target = target, replace = replace)

            removeAnime(animeId)
        }
    }

    fun removeAnime(animeId: Long) {
        screenModelScope.launchIO {
            val item = items.find { it.anime.id == animeId } ?: return@launchIO
            removeAnime(item)
            item.migrationScope.cancel()
            updateMigrationProgress()
        }
    }

    private fun removeAnime(item: MigratingAnime) {
        mutableState.update { it.copy(items = items.toPersistentList().remove(item)) }
    }

    override fun onDispose() {
        super.onDispose()
        items.forEach {
            it.migrationScope.cancel()
        }
    }

    fun showMigrateDialog(copy: Boolean) {
        mutableState.update { state ->
            state.copy(
                dialog = Dialog.Migrate(
                    copy = copy,
                    totalCount = items.size,
                    skippedCount = items.count { it.searchResult.value == SearchResult.NotFound },
                ),
            )
        }
    }

    fun showExitDialog() {
        mutableState.update {
            it.copy(dialog = Dialog.Exit)
        }
    }

    fun dismissDialog() {
        mutableState.update { it.copy(dialog = null) }
    }

    data class EpisodeInfo(
        val latestEpisode: Double?,
        val episodeCount: Int,
    )

    sealed interface Dialog {
        data class Migrate(val copy: Boolean, val totalCount: Int, val skippedCount: Int) : Dialog
        data class Progress(@FloatRange(0.0, 1.0) val progress: Float) : Dialog
        data object Exit : Dialog
    }

    data class State(
        val items: ImmutableList<MigratingAnime> = persistentListOf(),
        val finishedCount: Int = 0,
        val migrationComplete: Boolean = false,
        val dialog: Dialog? = null,
    ) {
        val animeIds: List<Long> = items.map { it.anime.id }
    }
}
