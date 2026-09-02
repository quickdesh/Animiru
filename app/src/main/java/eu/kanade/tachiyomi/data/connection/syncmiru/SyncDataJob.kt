// AM (SYNC) -->
package eu.kanade.tachiyomi.data.connection.syncmiru

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import dev.zacsweers.metro.Inject
import eu.kanade.domain.connection.SyncPreferences
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.util.system.cancelNotification
import eu.kanade.tachiyomi.util.system.isRunning
import eu.kanade.tachiyomi.util.system.setForegroundSafely
import eu.kanade.tachiyomi.util.system.workManager
import logcat.LogPriority
import mihon.app.di.AppGraph
import mihon.app.di.appGraph
import mihon.core.metro.metroGraph
import tachiyomi.core.common.util.system.logcat
import java.util.concurrent.TimeUnit

class SyncDataJob(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val graph: AppGraph = context.metroGraph()

    @Inject private lateinit var syncPreferences: SyncPreferences

    @Inject private lateinit var syncManager: SyncManager

    @Inject private lateinit var securityPreferences: SecurityPreferences

    private val notifier = SyncNotifier(context, securityPreferences)

    init {
        graph.inject(this)
    }

    override suspend fun doWork(): Result {
        if (tags.contains(TAG_AUTO)) {
            // Find a running manual worker. If exists, try again later
            if (context.workManager.isRunning(TAG_MANUAL)) {
                return Result.retry()
            }
        }

        setForegroundSafely()

        return try {
            syncManager.syncData()
            Result.success()
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e)
            notifier.showSyncError(e.message)
            Result.failure()
        } finally {
            context.cancelNotification(Notifications.ID_RESTORE_PROGRESS)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            Notifications.ID_RESTORE_PROGRESS,
            notifier.showSyncProgress().build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
    }

    companion object {
        private const val TAG_JOB = "SyncDataJob"
        private const val TAG_AUTO = "$TAG_JOB:auto"
        const val TAG_MANUAL = "$TAG_JOB:manual"

        fun isRunning(workManager: WorkManager): Boolean {
            return workManager.isRunning(TAG_JOB)
        }

        fun setupTask(context: Context, prefInterval: Int? = null) {
            val interval = prefInterval ?: context.appGraph.syncPreferences.syncInterval.get()

            if (interval > 0) {
                val request = PeriodicWorkRequestBuilder<SyncDataJob>(
                    interval.toLong(),
                    TimeUnit.MINUTES,
                    10,
                    TimeUnit.MINUTES,
                )
                    .addTag(TAG_JOB)
                    .addTag(TAG_AUTO)
                    .build()

                context.workManager.enqueueUniquePeriodicWork(TAG_AUTO, ExistingPeriodicWorkPolicy.UPDATE, request)
            } else {
                context.workManager.cancelUniqueWork(TAG_AUTO)
            }
        }

        fun startNow(workManager: WorkManager) {
            val wm = workManager
            if (wm.isRunning(TAG_JOB)) {
                // Already running either as a scheduled or manual job
                return
            }
            val request = OneTimeWorkRequestBuilder<SyncDataJob>()
                .addTag(TAG_JOB)
                .addTag(TAG_MANUAL)
                .build()
            workManager.enqueueUniqueWork(TAG_MANUAL, ExistingWorkPolicy.KEEP, request)
        }

        fun stop(context: Context) {
            val wm = context.workManager
            val workQuery = WorkQuery.Builder.fromTags(listOf(TAG_JOB, TAG_AUTO, TAG_MANUAL))
                .addStates(listOf(WorkInfo.State.RUNNING))
                .build()
            wm.getWorkInfos(workQuery).get()
                // Should only return one work but just in case
                .forEach {
                    wm.cancelWorkById(it.id)

                    // Re-enqueue cancelled scheduled work
                    if (it.tags.contains(TAG_AUTO)) {
                        setupTask(context)
                    }
                }
        }
    }
}
// <-- AM (SYNC)
