// app/src/main/java/com/app/cashflowfamily/utils/FCMService.kt

package com.app.cashflowfamily.utils

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.app.cashflowfamily.MainActivity
import com.app.cashflowfamily.R
import com.app.cashflowfamily.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "FCMService"
        private const val NOTIFICATION_ID_BASE = 5000
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received: ${remoteMessage.data}")

        remoteMessage.data.let { data ->
            val title = data["title"] ?: "Cashflow Family"
            val message = data["message"] ?: "Ada aktivitas baru di keluarga Anda"
            val type = data["type"] ?: "info"
            val notificationId = data["notificationId"]?.hashCode()
                ?.let { NOTIFICATION_ID_BASE + kotlin.math.abs(it % 1000) }
                ?: NOTIFICATION_ID_BASE

            showPushNotification(title, message, type, notificationId)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // Firebase bisa merotasi token kapan saja (reinstall, clear data, dsb),
        // termasuk saat belum ada user yang login -> abaikan kalau begitu.
        val userId = authRepository.getCurrentUser()?.uid
        if (userId.isNullOrBlank()) {
            Log.d(TAG, "Belum ada user login, token belum disimpan")
            return
        }

        scope.launch {
            authRepository.updateFcmToken(userId, token)
                .onSuccess { Log.d(TAG, "Token FCM tersimpan untuk user $userId") }
                .onFailure { e -> Log.e(TAG, "Gagal simpan token FCM", e) }
        }
    }

    private fun showPushNotification(
        title: String,
        message: String,
        type: String,
        notificationId: Int
    ) {
        // Pakai channel yang sama dengan notifikasi lokal supaya konsisten
        // dan tidak duplikat channel (channel-channel ini sudah dibuat di
        // NotificationHelper.createNotificationChannels() saat app start).
        val channelId = when (type) {
            "family_activity" -> NotificationHelper.CHANNEL_FAMILY_ACTIVITY
            "budget_warning" -> NotificationHelper.CHANNEL_BUDGET_WARNING
            "budget_over" -> NotificationHelper.CHANNEL_BUDGET_OVER
            "reminder" -> NotificationHelper.CHANNEL_REMINDER
            else -> NotificationHelper.CHANNEL_FAMILY_ACTIVITY
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_notification", true)
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
