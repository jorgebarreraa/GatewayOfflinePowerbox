package cl.powerbox.gateway.sync

import android.content.Context
import androidx.work.*
import cl.powerbox.gateway.util.Logger
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private const val SYNC_WORK_NAME = "gateway_sync_work"
    private const val PERIODIC_SYNC_WORK_NAME = "gateway_periodic_sync"

    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        Logger.d("✅ Periodic sync scheduled (every 15 minutes)")
    }

    fun syncNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        Logger.d("🔄 Immediate sync triggered")
    }

    fun cancelAllSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        Logger.d("❌ All sync work cancelled")
    }

    fun hasPendingSync(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(SYNC_WORK_NAME)
            .get()
        
        return workInfos.any { 
            it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED 
        }
    }
}