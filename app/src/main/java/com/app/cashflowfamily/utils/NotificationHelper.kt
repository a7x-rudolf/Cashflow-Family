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

    // ===== Notification Channels =====
    const val CHANNEL_REMINDER = "channel_reminder"
    const val CHANNEL_BUDGET_WARNING = "channel_budget_warning"
    const val CHANNEL_BUDGET_OVER = "channel_budget_over"
    const val CHANNEL_FAMILY_ACTIVITY = "channel_family_activity"

    // Channel BARU untuk event (dipisah dari budget bulanan agar user bisa
    // atur volume/prioritas notif event secara terpisah di sistem Android)
    const val CHANNEL_EVENT_WARNING = "channel_event_warning"
    const val CHANNEL_EVENT_OVER = "channel_event_over"

    // ===== Notification IDs =====
    const val NOTIFICATION_ID_REMINDER = 1001
    const val NOTIFICATION_ID_BUDGET_WARNING = 2001
    const val NOTIFICATION_ID_BUDGET_OVER = 3001
    const val NOTIFICATION_ID_FAMILY_ACTIVITY = 4001

    // BARU: base ID untuk event notifications
    const val NOTIFICATION_ID_EVENT_CATEGORY_WARNING = 5001
    const val NOTIFICATION_ID_EVENT_CATEGORY_OVER = 6001
    const val NOTIFICATION_ID_EVENT_WARNING = 7001
    const val NOTIFICATION_ID_EVENT_OVER = 8001

    /**
     * Buat notification channels (wajib untuk Android 8.0+)
     * Panggil sekali di Application.onCreate()
     */
    @SuppressLint("ObsoleteSdkInt")
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            // Channel 1: Reminder Harian (existing)
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                "Pengingat Harian",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengingat untuk mencatat transaksi harian"
            }

            // Channel 2: Budget Warning (existing)
            val warningChannel = NotificationChannel(
                CHANNEL_BUDGET_WARNING,
                "Peringatan Budget",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Peringatan saat budget mendekati limit"
            }

            // Channel 3: Budget Over (existing)
            val overChannel = NotificationChannel(
                CHANNEL_BUDGET_OVER,
                "Budget Terlampaui",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alert kritis saat budget terlampaui"
                enableVibration(true)
            }

            // Channel 4: Family Activity (existing)
            val familyChannel = NotificationChannel(
                CHANNEL_FAMILY_ACTIVITY,
                "Aktivitas Keluarga",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi saat anggota keluarga menambah transaksi"
            }

            // Channel 5: Event Warning (BARU)
            val eventWarningChannel = NotificationChannel(
                CHANNEL_EVENT_WARNING,
                "Peringatan Budget Event",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Peringatan saat budget event/wedding mendekati limit"
            }

            // Channel 6: Event Over (BARU)
            val eventOverChannel = NotificationChannel(
                CHANNEL_EVENT_OVER,
                "Budget Event Terlampaui",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alert kritis saat budget event terlampaui"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(warningChannel)
            notificationManager.createNotificationChannel(overChannel)
            notificationManager.createNotificationChannel(familyChannel)
            notificationManager.createNotificationChannel(eventWarningChannel)
            notificationManager.createNotificationChannel(eventOverChannel)
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

        val notificationId = NOTIFICATION_ID_FAMILY_ACTIVITY +
                (System.currentTimeMillis() % 1000).toInt()

        notificationManager.notify(notificationId, notification)
        android.util.Log.d("NotificationHelper", "Notification posted with ID: $notificationId")
    }

    // ============================================================
    // EVENT MODULE — v1.1.0
    // ============================================================

    /**
     * Tampilkan warning saat KATEGORI di dalam event mencapai 80%
     */
    fun showEventCategoryWarning(
        context: Context,
        eventName: String,
        categoryName: String,
        percentage: Int,
        spent: Double,
        budget: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            10,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EVENT_WARNING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Peringatan Budget: $categoryName")
            .setContentText("$eventName · $categoryName sudah $percentage%")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Event: $eventName\n" +
                                "Kategori: $categoryName\n\n" +
                                "${CurrencyFormatter.formatRupiah(spent)} dari ${CurrencyFormatter.formatRupiah(budget)} ($percentage%)\n\n" +
                                "Perhatikan pengeluaran kategori ini agar tidak melebihi budget."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // ID unik per event+kategori agar tidak override notif dari event/kategori lain
        val uniqueKey = "$eventName-$categoryName".hashCode()
        notificationManager.notify(
            NOTIFICATION_ID_EVENT_CATEGORY_WARNING + uniqueKey,
            notification
        )
    }

    /**
     * Tampilkan alert saat KATEGORI di dalam event terlampaui (>100%)
     */
    fun showEventCategoryOver(
        context: Context,
        eventName: String,
        categoryName: String,
        percentage: Int,
        spent: Double,
        budget: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            11,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val overAmount = spent - budget

        val notification = NotificationCompat.Builder(context, CHANNEL_EVENT_OVER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("BUDGET TERLAMPAUI: $categoryName")
            .setContentText("$eventName · Melebihi ${CurrencyFormatter.formatRupiah(overAmount)}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Event: $eventName\n" +
                                "Kategori $categoryName sudah melampaui budget!\n\n" +
                                "Terpakai: ${CurrencyFormatter.formatRupiah(spent)}\n" +
                                "Budget: ${CurrencyFormatter.formatRupiah(budget)}\n" +
                                "Terlampaui: ${CurrencyFormatter.formatRupiah(overAmount)} ($percentage%)\n\n" +
                                "Segera evaluasi pengeluaran atau transfer budget dari kategori lain."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val uniqueKey = "$eventName-$categoryName".hashCode()
        notificationManager.notify(
            NOTIFICATION_ID_EVENT_CATEGORY_OVER + uniqueKey,
            notification
        )
    }

    /**
     * Tampilkan warning saat TOTAL EVENT mencapai 80%
     */
    fun showEventWarning(
        context: Context,
        eventName: String,
        percentage: Int,
        spent: Double,
        budget: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            12,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EVENT_WARNING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Peringatan Budget Event: $eventName")
            .setContentText("Total event sudah $percentage%")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Event: $eventName\n\n" +
                                "Total pengeluaran event sudah mencapai:\n" +
                                "${CurrencyFormatter.formatRupiah(spent)} dari ${CurrencyFormatter.formatRupiah(budget)} ($percentage%)\n\n" +
                                "Cek kembali alokasi budget per kategori Anda."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        notificationManager.notify(
            NOTIFICATION_ID_EVENT_WARNING + eventName.hashCode(),
            notification
        )
    }

    /**
     * Tampilkan alert saat TOTAL EVENT terlampaui (>100%)
     */
    fun showEventOver(
        context: Context,
        eventName: String,
        percentage: Int,
        spent: Double,
        budget: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            13,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val overAmount = spent - budget

        val notification = NotificationCompat.Builder(context, CHANNEL_EVENT_OVER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("EVENT OVER BUDGET: $eventName")
            .setContentText("Total event melebihi ${CurrencyFormatter.formatRupiah(overAmount)}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Event: $eventName\n\n" +
                                "Total pengeluaran event SUDAH MELAMPAUI budget!\n\n" +
                                "Terpakai: ${CurrencyFormatter.formatRupiah(spent)}\n" +
                                "Budget: ${CurrencyFormatter.formatRupiah(budget)}\n" +
                                "Terlampaui: ${CurrencyFormatter.formatRupiah(overAmount)} ($percentage%)\n\n" +
                                "Segera evaluasi pengeluaran atau tambah total budget event Anda."
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
            NOTIFICATION_ID_EVENT_OVER + eventName.hashCode(),
            notification
        )
    }
}