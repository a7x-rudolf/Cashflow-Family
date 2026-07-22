package com.app.cashflowfamily.utils.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.cashflowfamily.data.preferences.NotificationPreferences
import com.app.cashflowfamily.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationPreferences: NotificationPreferences
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Cek apakah user enable reminder
            val isEnabled = notificationPreferences.isDailyReminderEnabled.first()

            if (isEnabled) {
                NotificationHelper.showDailyReminder(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}