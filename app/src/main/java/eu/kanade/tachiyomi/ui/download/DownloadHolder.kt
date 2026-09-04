package eu.kanade.tachiyomi.ui.download

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import eu.davidea.viewholders.FlexibleViewHolder
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.databinding.DownloadItemBinding
import eu.kanade.tachiyomi.util.view.popupMenu
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.animiru.AMMR
import tachiyomi.i18n.aniyomi.AYMR

/**
 * Class used to hold the data of a download.
 * All the elements from the layout file "download_item" are available in this class.
 *
 * @param view the inflated view for this holder.
 * @constructor creates a new download holder.
 */
class DownloadHolder(private val view: View, val adapter: DownloadAdapter) :
    FlexibleViewHolder(view, adapter) {

    private val binding = DownloadItemBinding.bind(view)

    init {
        setDragHandleView(binding.reorder)
        binding.menu.setOnClickListener { it.post { showPopupMenu(it) } }
    }

    private lateinit var download: Download

    /**
     * Binds this holder with the given category.
     *
     * @param category The category to bind.
     */
    fun bind(download: Download) {
        this.download = download
        // Update the episode name.
        binding.episodeTitle.text = download.episode.name

        // Update the anime title
        binding.animeFullTitle.text = download.anime.title

        // Update the progress bar and the download progress
        val video = download.video
        if (video == null) {
            binding.downloadProgress.progress = 0
            binding.downloadProgress.max = 1
            binding.downloadProgressText.text = ""
        } else {
            binding.downloadProgress.max = 100
            notifyProgress()
            notifyDownloadProgress()
        }
    }

    /**
     * Updates the progress bar of the download.
     */
    fun notifyProgress() {
        if (binding.downloadProgress.max == 1) {
            // AY -->
            binding.downloadProgress.max = 100
        }
        if (download.progress == 0) {
            binding.downloadProgress.isIndeterminate = true
        } else {
            binding.downloadProgress.isIndeterminate = false
            binding.downloadProgress.setProgressCompat(download.progress, true)
        }
        // <-- AY
    }

    /**
     * Updates the text field of download progress.
     */
    fun notifyDownloadProgress() {
        // AY -->
        binding.downloadProgressText.text = if (download.progress == 0) {
            view.context.stringResource(AMMR.strings.am_update_check_notification_download_in_progress)
        } else {
            view.context.stringResource(AYMR.strings.episode_download_progress, download.progress)
        }
        // <-- AY
    }

    override fun onItemReleased(position: Int) {
        super.onItemReleased(position)
        adapter.downloadItemListener.onItemReleased(position)
        binding.container.isDragged = false
    }

    override fun onActionStateChanged(position: Int, actionState: Int) {
        super.onActionStateChanged(position, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            binding.container.isDragged = true
        }
    }

    private fun showPopupMenu(view: View) {
        view.popupMenu(
            menuRes = R.menu.download_single,
            initMenu = {
                findItem(R.id.move_to_top).isVisible = bindingAdapterPosition > 1
                findItem(R.id.move_to_bottom).isVisible =
                    bindingAdapterPosition != adapter.itemCount - 1
            },
            onMenuItemClick = {
                adapter.downloadItemListener.onMenuItemClick(bindingAdapterPosition, this)
            },
        )
    }
}
