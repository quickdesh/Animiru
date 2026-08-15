package eu.kanade.tachiyomi.di

import android.app.Application
import animiru.domain.player.service.AdvancedPlayerPreferences
import animiru.domain.player.service.AudioPreferences
import animiru.domain.player.service.DecoderPreferences
import animiru.domain.player.service.GesturePreferences
import animiru.domain.player.service.PlayerPreferences
import animiru.domain.player.service.SubtitlePreferences
import aniyomi.core.common.torrent.TorrentPreferences
import eu.kanade.domain.base.BasePreferences
import eu.kanade.domain.connection.SyncPreferences
import eu.kanade.domain.connection.service.ConnectionPreferences
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.domain.track.service.TrackPreferences
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import eu.kanade.tachiyomi.network.NetworkPreferences
import eu.kanade.tachiyomi.util.system.isDebugBuildType
import tachiyomi.core.common.preference.AndroidPreferenceStore
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.storage.AndroidStorageFolderProvider
import tachiyomi.domain.backup.service.BackupPreferences
import tachiyomi.domain.download.service.DownloadPreferences
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.storage.service.StoragePreferences
import tachiyomi.domain.upcoming.service.UpcomingPreferences
import tachiyomi.domain.updates.service.UpdatesPreferences
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get

class PreferenceModule(val app: Application) : InjektModule {

    override fun InjektRegistrar.registerInjectables() {
        addSingletonFactory<PreferenceStore> {
            AndroidPreferenceStore(app)
        }
        addSingletonFactory {
            NetworkPreferences(
                preferenceStore = get(),
                verboseLoggingDefault = isDebugBuildType,
            )
        }
        addSingletonFactory {
            SourcePreferences(get())
        }
        addSingletonFactory {
            SecurityPreferences(get())
        }
        addSingletonFactory {
            LibraryPreferences(get())
        }
        addSingletonFactory {
            UpcomingPreferences(get())
        }
        addSingletonFactory {
            UpdatesPreferences(get())
        }
        // AY -->
        addSingletonFactory {
            PlayerPreferences(get())
        }
        addSingletonFactory {
            GesturePreferences(get())
        }
        addSingletonFactory {
            DecoderPreferences(get())
        }
        addSingletonFactory {
            SubtitlePreferences(get())
        }
        addSingletonFactory {
            AudioPreferences(get())
        }
        addSingletonFactory {
            TorrentPreferences(get())
        }
        addSingletonFactory {
            AdvancedPlayerPreferences(get())
        }
        // <-- AY
        addSingletonFactory {
            TrackPreferences(get())
        }
        // AM (CONNECTION) -->
        addSingletonFactory {
            ConnectionPreferences(get())
        }
        // <-- AM (CONNECTION)
        addSingletonFactory {
            DownloadPreferences(get())
        }
        addSingletonFactory {
            BackupPreferences(get())
        }
        // AM (SYNC) -->
        addSingletonFactory {
            SyncPreferences(get())
        }
        // <-- AM (SYNC)
        addSingletonFactory {
            StoragePreferences(
                folderProvider = get<AndroidStorageFolderProvider>(),
                preferenceStore = get(),
            )
        }
        addSingletonFactory {
            UiPreferences(get())
        }
        addSingletonFactory {
            BasePreferences(app, get())
        }
    }
}
