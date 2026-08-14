package eu.kanade.tachiyomi.ui.anime

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.asDrawable
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import eu.kanade.domain.anime.interactor.UpdateAnime
import eu.kanade.tachiyomi.data.cache.BackgroundCache
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.saver.Image
import eu.kanade.tachiyomi.data.saver.ImageSaver
import eu.kanade.tachiyomi.data.saver.Location
import eu.kanade.tachiyomi.util.editBackground
import eu.kanade.tachiyomi.util.editCover
import eu.kanade.tachiyomi.util.system.getBitmapOrNull
import eu.kanade.tachiyomi.util.system.toShareIntent
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.LogPriority
import mihon.core.viewmodel.StateViewModel
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.lang.withUIContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.anime.interactor.GetAnime
import tachiyomi.domain.anime.model.Anime
import tachiyomi.i18n.MR
import tachiyomi.i18n.aniyomi.AYMR
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnimeImageViewModel(
    private val animeId: Long,
    private val getAnime: GetAnime = Injekt.get(),
    private val imageSaver: ImageSaver = Injekt.get(),
    private val coverCache: CoverCache = Injekt.get(),
    // AY -->
    private val backgroundCache: BackgroundCache = Injekt.get(),
    // <-- AY
    private val updateAnime: UpdateAnime = Injekt.get(),

    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    // AY -->
    val pagerState: PagerState = PagerState(pageCount = { 2 }),
    // <-- AY
) : StateViewModel<Anime?>(null) {

    companion object {
        val ANIME_ID_KEY = CreationExtras.Key<Long>()

        val Factory = viewModelFactory {
            initializer {
                AnimeImageViewModel(
                    animeId = get(ANIME_ID_KEY)!!,
                )
            }
        }
    }

    // AY -->
    private val isCover: Boolean
        get() = pagerState.currentPage != 1
    // <-- AY

    init {
        viewModelScope.launchIO {
            getAnime.subscribe(animeId)
                .collect { newAnime -> mutableState.update { newAnime } }
        }
    }

    fun saveImage(context: Context) {
        // AY -->
        val savedStringResource = if (isCover) {
            MR.strings.cover_saved
        } else {
            AYMR.strings.background_saved
        }
        val errorSavingStringResource = if (isCover) {
            MR.strings.error_saving_cover
        } else {
            AYMR.strings.error_saving_background
        }
        // <-- AY
        viewModelScope.launch {
            try {
                saveImageInternal(context, temp = false)
                snackbarHostState.showSnackbar(
                    context.stringResource(savedStringResource),
                    withDismissAction = true,
                )
            } catch (e: Throwable) {
                logcat(LogPriority.ERROR, e)
                snackbarHostState.showSnackbar(
                    context.stringResource(errorSavingStringResource),
                    withDismissAction = true,
                )
            }
        }
    }

    fun shareImage(context: Context) {
        // AY -->
        val errorSharingStringResource = if (isCover) {
            MR.strings.error_sharing_cover
        } else {
            AYMR.strings.error_sharing_background
        }
        // <-- AY
        viewModelScope.launch {
            try {
                val uri = saveImageInternal(context, temp = true) ?: return@launch
                withUIContext {
                    context.startActivity(uri.toShareIntent(context))
                }
            } catch (e: Throwable) {
                logcat(LogPriority.ERROR, e)
                snackbarHostState.showSnackbar(
                    context.stringResource(errorSharingStringResource),
                    withDismissAction = true,
                )
            }
        }
    }

    /**
     * Save anime image Bitmap to picture or temporary share directory.
     *
     * @param context The context for building and executing the ImageRequest
     * @return the uri to saved file
     */
    private suspend fun saveImageInternal(context: Context, temp: Boolean): Uri? {
        val anime = state.value ?: return null
        val req = ImageRequest.Builder(context)
            .data(anime)
            .size(Size.ORIGINAL)
            .build()

        return withIOContext {
            val result = context.imageLoader.execute(req).image?.asDrawable(context.resources)

            // TODO: Handle animated image
            val bitmap = result?.getBitmapOrNull() ?: return@withIOContext null
            imageSaver.save(
                Image.Cover(
                    bitmap = bitmap,
                    // AY -->
                    name = if (isCover) "${anime.title}-cover" else "${anime.title}-background",
                    // <-- AY
                    location = if (temp) Location.Cache else Location.Pictures.create(),
                ),
            )
        }
    }

    /**
     * Update image with local file.
     *
     * @param context Context.
     * @param data uri of the image resource.
     */
    fun editImage(context: Context, data: Uri) {
        val anime = state.value ?: return
        viewModelScope.launchIO {
            context.contentResolver.openInputStream(data)?.use {
                try {
                    if (isCover) {
                        anime.editCover(Injekt.get(), it, updateAnime, coverCache)
                    } else {
                        // AY -->
                        anime.editBackground(Injekt.get(), it, updateAnime, backgroundCache)
                        // <-- AY
                    }
                    notifyImageUpdated(context)
                } catch (e: Exception) {
                    notifyFailedImageUpdate(context, e)
                }
            }
        }
    }

    fun deleteCustomImage(context: Context) {
        val animeId = state.value?.id ?: return
        viewModelScope.launchIO {
            try {
                if (isCover) {
                    coverCache.deleteCustomCover(animeId)
                    updateAnime.awaitUpdateCoverLastModified(animeId)
                } else {
                    backgroundCache.deleteCustomBackground(animeId)
                    updateAnime.awaitUpdateBackgroundLastModified(animeId)
                }
                notifyImageUpdated(context)
            } catch (e: Exception) {
                notifyFailedImageUpdate(context, e)
            }
        }
    }

    private fun notifyImageUpdated(context: Context) {
        // AY -->
        val updatedStringResource = if (isCover) {
            MR.strings.cover_updated
        } else {
            AYMR.strings.background_updated
        }
        // <-- AY
        viewModelScope.launch {
            snackbarHostState.showSnackbar(
                context.stringResource(updatedStringResource),
                withDismissAction = true,
            )
        }
    }

    private fun notifyFailedImageUpdate(context: Context, e: Throwable) {
        // AY -->
        val updateFailedStringResource = if (isCover) {
            MR.strings.notification_cover_update_failed
        } else {
            AYMR.strings.notification_background_update_failed
        }
        // <-- AY
        viewModelScope.launch {
            snackbarHostState.showSnackbar(
                context.stringResource(updateFailedStringResource),
                withDismissAction = true,
            )
            logcat(LogPriority.ERROR, e)
        }
    }
}
