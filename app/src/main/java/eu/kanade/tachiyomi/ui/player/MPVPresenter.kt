package eu.kanade.tachiyomi.ui.player

import android.os.Bundle
import eu.kanade.tachiyomi.animesource.AnimeSourceManager
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.data.cache.AnimeCoverCache
import eu.kanade.tachiyomi.data.database.AnimeDatabaseHelper
import eu.kanade.tachiyomi.data.database.models.Anime
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.download.AnimeDownloadManager
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.track.job.DelayedTrackingStore
import eu.kanade.tachiyomi.ui.base.presenter.BasePresenter
import eu.kanade.tachiyomi.util.episode.getEpisodeSort
import eu.kanade.tachiyomi.util.lang.launchIO
import eu.kanade.tachiyomi.util.system.logcat
import logcat.LogPriority
import rx.android.schedulers.AndroidSchedulers
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MPVPresenter(
    private val db: AnimeDatabaseHelper = Injekt.get(),
    private val sourceManager: AnimeSourceManager = Injekt.get(),
    private val downloadManager: AnimeDownloadManager = Injekt.get(),
    private val coverCache: AnimeCoverCache = Injekt.get(),
    private val preferences: PreferencesHelper = Injekt.get(),
    private val delayedTrackingStore: DelayedTrackingStore = Injekt.get(),
) : BasePresenter<MPVActivity>() {
    /**
     * The anime loaded in the player. It can be null when instantiated for a short time.
     */
    var anime: Anime? = null
        private set

    /**
     * The episode id of the currently loaded episode. Used to restore from process kill.
     */
    private var episodeId = -1L

    private var currentEpisode: Episode? = null

    private var currentVideoList: List<Video>? = null

    /**
     * Episode list for the active anime. It's retrieved lazily and should be accessed for the first
     * time in a background thread to avoid blocking the UI.
     */
    private val episodeList by lazy {
        val anime = anime!!
        val dbEpisodes = db.getEpisodes(anime).executeAsBlocking()

        val selectedEpisode = dbEpisodes.find { it.id == episodeId }
            ?: error("Requested episode of id $episodeId not found in episode list")

        val episodesForPlayer = when {
            preferences.skipRead() || preferences.skipFiltered() -> {
                val filteredEpisodes = dbEpisodes.filterNot {
                    when {
                        preferences.skipRead() && it.seen -> true
                        preferences.skipFiltered() -> {
                            anime.seenFilter == Anime.EPISODE_SHOW_SEEN && !it.seen ||
                                anime.seenFilter == Anime.EPISODE_SHOW_UNSEEN && it.seen ||
                                anime.downloadedFilter == Anime.EPISODE_SHOW_DOWNLOADED && !downloadManager.isEpisodeDownloaded(it, anime) ||
                                anime.downloadedFilter == Anime.EPISODE_SHOW_NOT_DOWNLOADED && downloadManager.isEpisodeDownloaded(it, anime) ||
                                anime.bookmarkedFilter == Anime.EPISODE_SHOW_BOOKMARKED && !it.bookmark ||
                                anime.bookmarkedFilter == Anime.EPISODE_SHOW_NOT_BOOKMARKED && it.bookmark
                        }
                        else -> false
                    }
                }

                if (filteredEpisodes.any { it.id == episodeId }) {
                    filteredEpisodes
                } else {
                    filteredEpisodes + listOf(selectedEpisode)
                }
            }
            else -> dbEpisodes
        }

        episodesForPlayer
            .sortedWith(getEpisodeSort(anime, sortDescending = false))
    }

    private var hasTrackers: Boolean = false
    private val checkTrackers: (Anime) -> Unit = { anime ->
        val tracks = db.getTracks(anime).executeAsBlocking()

        hasTrackers = tracks.size > 0
    }

    private val incognitoMode = preferences.incognitoMode().get()

    /**
     * Called when the presenter is created. It retrieves the saved active episode if the process
     * was restored.
     */
    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        if (savedState != null) {
            episodeId = savedState.getLong(::episodeId.name, -1)
        }
    }

    /**
     * Called when the presenter is destroyed. It saves the current progress and cleans up
     * references on the currently active episodes.
     */
    override fun onDestroy() {
        super.onDestroy()
        /*val currentEpisode = currentEpisode
        if (currentEpisode != null) {
            saveEpisodeProgress(currentEpisode)
            saveEpisodeHistory(currentEpisode)
        }*/
    }

    /**
     * Called when the presenter instance is being saved. It saves the currently active episode
     * id and the last page seen.
     */
    override fun onSave(state: Bundle) {
        super.onSave(state)
        val currentEpisode = currentEpisode
        if (currentEpisode != null) {
            // saveEpisodeProgress(currentEpisode)
            // saveEpisodeHistory(currentEpisode)
            state.putLong(::episodeId.name, currentEpisode.id!!)
        }
    }

    /**
     * Whether this presenter is initialized yet.
     */
    fun needsInit(): Boolean {
        return anime == null
    }

    /**
     * Initializes this presenter with the given [animeId] and [initialEpisodeId]. This method will
     * fetch the anime from the database and initialize the initial episode.
     */
    fun init(animeId: Long, initialEpisodeId: Long) {
        if (!needsInit()) return

        db.getAnime(animeId).asRxObservable()
            .first()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { init(it, initialEpisodeId) }
            .subscribeFirst(
                { _, _ ->
                    // Ignore onNext event
                },
                MPVActivity::setInitialEpisodeError
            )
    }

    /**
     * Initializes this presenter with the given [anime] and [initialEpisodeId]. This method will
     * set the episode loader, view subscriptions and trigger an initial load.
     */
    private fun init(anime: Anime, initialEpisodeId: Long) {
        if (!needsInit()) return

        this.anime = anime
        if (episodeId == -1L) episodeId = initialEpisodeId

        checkTrackers(anime)

        val source = sourceManager.getOrStub(anime.source)

        currentEpisode = episodeList.first { initialEpisodeId == it.id }
        launchIO {
            try {
                val currentEpisode = currentEpisode ?: throw Exception("bruh")
                EpisodeLoader.getLinks(currentEpisode, anime, source)
                    .subscribeFirst(
                        { activity, it ->
                            currentVideoList = it
                            activity.setVideoList(it)
                        },
                        MPVActivity::setInitialEpisodeError
                    )
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e) { e.message ?: "error getting links" }
            }
        }
    }
}
