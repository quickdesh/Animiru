package eu.kanade.domain.source.model

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.extension.model.Extension
import tachiyomi.domain.source.model.Source
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

val Source.icon: ImageBitmap?
    get() {
        return Injekt.get<ExtensionManager>().getAppIconForSource(id)
            ?.toBitmap()
            ?.asImageBitmap()
    }

// AM (BROWSE) -->
private val sourceIdToExtensionMap: MutableMap<Long, Extension.Installed> by lazy {
    val map = mutableMapOf<Long, Extension.Installed>()
    Injekt.get<ExtensionManager>()
        .installedExtensionsFlow
        .value
        .forEach { ext ->
            ext.sources.forEach { source ->
                map[source.id] = ext
            }
        }
    map
}

fun updateSourceIdToExtensionMap() {
    sourceIdToExtensionMap.clear()
    Injekt.get<ExtensionManager>()
        .installedExtensionsFlow
        .value
        .forEach { ext ->
            ext.sources.forEach { source ->
                sourceIdToExtensionMap[source.id] = ext
            }
        }
}

val Source.installedExtension: Extension.Installed?
    get() {
        return sourceIdToExtensionMap[id]
    }
// <-- AM (BROWSE)
