package com.example.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.GenzPluseApplication
import com.example.domain.model.SyncState

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Enforce bounded retries to prevent infinite loops or battery drain
        if (runAttemptCount > 3) {
            return Result.failure()
        }

        val app = applicationContext as? GenzPluseApplication ?: return Result.failure()
        val authRepo = app.container.authRepository
        val syncManager = app.container.syncManager

        val uid = authRepo.getAuthenticatedUid()
        // If user is logged out, avoid running background synchronization
        if (uid.isNullOrBlank()) {
            return Result.success()
        }

        return try {
            val syncResult = syncManager.syncAll(uid)
            when (syncResult.state) {
                SyncState.IDLE,
                SyncState.SUCCESS,
                SyncState.OFFLINE -> Result.success()
                SyncState.SYNCING -> Result.success()
                SyncState.ERROR -> {
                    if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val TAG_PERIODIC_SYNC = "genzpluse_periodic_sync"
        const val TAG_IMMEDIATE_SYNC = "genzpluse_immediate_sync"
    }
}
