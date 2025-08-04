package eu.kanade.tachiyomi.data.backup.create.creators

import android.content.Context
import android.content.pm.PackageManager
import eu.kanade.tachiyomi.data.backup.models.BackupExtension
import eu.kanade.tachiyomi.extension.ExtensionManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.File

class ExtensionsBackupCreator(
    private val context: Context,
    private val extensionManager: ExtensionManager = Injekt.get(),
) {

    operator fun invoke(): List<BackupExtension> {
        val installedExtensions = mutableListOf<BackupExtension>()
        extensionManager.installedExtensionsFlow.value.forEach {
            val packageName = it.pkgName
            val apk = File(
                context.packageManager
                    .getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA,
                    ).publicSourceDir,
            ).readBytes()
            installedExtensions.add(
                BackupExtension(packageName, apk),
            )
        }
        return installedExtensions
    }
}
