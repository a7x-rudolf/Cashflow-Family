package com.app.cashflowfamily.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.cashflowfamily.MainActivity
import com.app.cashflowfamily.R

object NotificationHelper {

    // Notification Channels
    const val CHANNEL_REMINDER = "channel_reminder"
    const val CHANNEL_BUDGET_WARNING = "channel_budget_warning"
    const val CHANNEL_BUDGET_OVER = "channel_budget_over"

    // Notification IDs (unique untuk setiap jenis)
    const val NOTIFICATION_ID_REMINDER = 1001
    const val NOTIFICATION_ID_BUDGET_WARNING = 2001
    const val NOTIFICATION_ID_BUDGET_OVER = 3001

    // Channel baru untuk transaksi anggota keluarga
    const val CHANNEL_FAMILY_ACTIVITY = "channel_family_activity"

    // Notification ID base
    const val NOTIFICATION_ID_FAMILY_ACTIVITY = 4001

    /**
     * Buat notification channels (wajib untuk Android 8.0+)
     * Panggil sekali di Application.onCreate()
     */
    @SuppressLint("ObsoleteSdkInt")
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            // Channel 1: Reminder Harian (sudah ada)
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                "Pengingat Harian",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengingat untuk mencatat transaksi harian"
            }

            // Channel 2: Budget Warning (sudah ada)
            val warningChannel = NotificationChannel(
                CHANNEL_BUDGET_WARNING,
                "Peringatan Budget",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Peringatan saat budget mendekati limit"
            }

            // Channel 3: Budget Over (sudah ada)
            val overChannel = NotificationChannel(
                CHANNEL_BUDGET_OVER,
                "Budget Terlampaui",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alert kritis saat budget terlampaui"
                enableVibration(true)
            }

            // Channel 4: Family Activity (BARU)
            val familyChannel = NotificationChannel(
                CHANNEL_FAMILY_ACTIVITY,
                "Aktivitas Keluarga",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi saat anggota keluarga menambah transaksi"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(warningChannel)
            notificationManager.createNotificationChannel(overChannel)
            notificationManager.createNotificationChannel(familyChannel)
        }
    }

    /**
     * Tampilkan notifikasi reminder harian
     */
    fun showDailyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Cashflow Family")
            .setContentText("Sudahkah Anda mencatat transaksi hari ini?")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Jangan lupa catat pemasukan & pengeluaran hari ini untuk keuangan keluarga yang lebih teratur.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    /**
     * Tampilkan warning saat budget mencapai 80%
     */
    fun showBudgetWarning(
        context: Context,
        category: String,
        percentage: Int,
        spent: Double,
        budget: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_WARNING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Peringatan Budget: $category")
            .setContentText("Budget $category sudah mencapai $percentage%")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Pengeluaran kategori $category:\n" +
                                "${CurrencyFormatter.formatRupiah(spent)} dari ${CurrencyFormatter.formatRupiah(budget)} ($percentage%)\n\n" +
                                "Perhatikan pengeluaran Anda agar tidak melebihi budget."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // ID unik per kategori agar tidak override
        notificationManager.notify(
            NOTIFICATION_ID_BUDGET_WARNING + category.hashCode(),
            notification
        )
    }

    /**
     * Tampilkan alert saat budget terlampaui (>100%)
     */
    fun showBudgetOver(
        context: Context,
        category: String,
        percentage: Int,
        spent: Double,
        budget: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val overAmount = spent - budget

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_OVER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("BUDGET TERLAMPAUI: $category")
            .setContentText("Pengeluaran melebihi budget ${CurrencyFormatter.formatRupiah(overAmount)}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Kategori $category sudah melampaui budget!\n\n" +
                                "Terpakai: ${CurrencyFormatter.formatRupiah(spent)}\n" +
                                "Budget: ${CurrencyFormatter.formatRupiah(budget)}\n" +
                                "Terlampaui: ${CurrencyFormatter.formatRupiah(overAmount)} ($percentage%)\n\n" +
                                "Segera evaluasi pengeluaran atau sesuaikan budget Anda."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        notificationManager.notify(
            NOTIFICATION_ID_BUDGET_OVER + category.hashCode(),
            notification
        )
    }

    /**
     * Notifikasi saat anggota keluarga menambah transaksi baru
     */
    fun showFamilyTransactionNotification(
        context: Context,
        userName: String,
        transactionType: String,
        amount: Double,
        category: String
    ) {
        android.util.Log.d("NotificationHelper", "showFamilyTransactionNotification called")
        android.util.Log.d("NotificationHelper", "userName: $userName, amount: $amount")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val typeText = if (transactionType == "income") "pemasukan" else "pengeluaran"
        val emoji = if (transactionType == "income") "↓" else "↑"

        val title = "Aktivitas Keluarga"
        val shortContent = "$userName menambah $typeText"
        val bigContent = "$emoji $userName menambahkan $typeText baru\n" +
                "Kategori: $category\n" +
                "Jumlah: ${CurrencyFormatter.formatRupiah(amount)}"

        val notification = NotificationCompat.Builder(context, CHANNEL_FAMILY_ACTIVITY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigContent)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Gunakan random ID agar setiap notif tampil terpisah
        val notificationId = NOTIFICATION_ID_FAMILY_ACTIVITY +
                (System.currentTimeMillis() % 1000).toInt()

        notificationManager.notify(notificationId, notification)
        android.util.Log.d("NotificationHelper", "Notification posted with ID: $notificationId")
    }
}