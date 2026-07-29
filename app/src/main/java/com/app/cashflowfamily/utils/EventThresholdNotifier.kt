@file:Suppress("AddExplicitTargetToParameterAnnotation")

package com.app.cashflowfamily.utils

import android.content.Context
import android.util.Log
import com.app.cashflowfamily.data.model.Notification
import com.app.cashflowfamily.data.preferences.NotificationPreferences
import com.app.cashflowfamily.data.repository.EventRepository
import com.app.cashflowfamily.data.repository.FamilyRepository
import com.app.cashflowfamily.data.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cek threshold budget event & kategori setelah setiap perubahan
 * (tambah/edit transaksi, transfer budget). Mengikuti pola
 * BudgetThresholdNotifier yang sudah proven working.
 *
 * Logika:
 * - Cek dua level: kategori (per-kategori) DAN event (total keseluruhan)
 * - Tier 80: warning, Tier 100: over budget
 * - Anti-spam: kirim notif hanya kalau tier saat ini > lastNotifiedPercentage
 * - Reset ke 0 sudah di-handle di EventRepository saat spent turun
 *   (di updateTransaction/deleteTransaction/transferBudget)
 */
@Singleton
class EventThresholdNotifier @Inject constructor(
    private val eventRepository: EventRepository,
    private val familyRepository: FamilyRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationPreferences: NotificationPreferences,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "EventThresholdNotifier"
    }

    /**
     * Cek threshold untuk satu event + satu kategori yang baru saja berubah.
     * Dipanggil dari EventTransactionViewModel setelah add/update transaksi sukses.
     *
     * @param eventId ID event yang berubah
     * @param categoryId ID kategori yang berubah (opsional, null = skip cek kategori)
     * @param actorUserId User yang trigger perubahan (untuk push notification)
     */
    suspend fun checkAndNotify(
        eventId: String,
        categoryId: String? = null,
        actorUserId: String = ""
    ) {
        if (eventId.isBlank()) return

        try {
            // Cek user preference — reuse setting yang sama dengan budget warning
            val isEnabled = notificationPreferences.isBudgetWarningEnabled.first()
            if (!isEnabled) {
                Log.d(TAG, "Budget warning disabled by user, skip check")
                return
            }

            val event = eventRepository.getEventById(eventId).getOrNull()
                ?: run {
                    Log.w(TAG, "Event not found: $eventId")
                    return
                }

            // === CHECK CATEGORY LEVEL ===
            if (!categoryId.isNullOrBlank()) {
                checkCategory(event.familyId, eventId, categoryId, actorUserId)
            }

            // === CHECK EVENT LEVEL ===
            checkEvent(event, actorUserId)

        } catch (e: Exception) {
            Log.e(TAG, "Error in checkAndNotify", e)
        }
    }

    /**
     * Cek threshold untuk semua kategori dalam event.
     * Dipakai saat transfer budget (karena bisa memicu perubahan di 2 kategori).
     */
    suspend fun checkAllCategoriesAndEvent(
        eventId: String,
        actorUserId: String = ""
    ) {
        if (eventId.isBlank()) return

        try {
            val isEnabled = notificationPreferences.isBudgetWarningEnabled.first()
            if (!isEnabled) return

            val event = eventRepository.getEventById(eventId).getOrNull() ?: return
            val categories = eventRepository.getCategories(eventId).getOrElse { emptyList() }

            categories.forEach { cat ->
                checkCategory(event.familyId, eventId, cat.categoryId, actorUserId)
            }

            checkEvent(event, actorUserId)

        } catch (e: Exception) {
            Log.e(TAG, "Error in checkAllCategoriesAndEvent", e)
        }
    }

    private suspend fun checkCategory(
        familyId: String,
        eventId: String,
        categoryId: String,
        actorUserId: String
    ) {
        val category = eventRepository.getCategoryById(eventId, categoryId).getOrNull()
            ?: return

        if (category.effectiveBudget <= 0) return

        val percentage = category.budgetPercentage.toInt()
        val tier = computeTier(percentage)

        // Skip kalau belum ada threshold tercapai, atau sudah pernah dinotifikasi
        if (tier == 0 || tier <= category.lastNotifiedPercentage) return

        val isOver = tier >= 100
        val eventName = eventRepository.getEventById(eventId).getOrNull()?.name ?: "Event"

        Log.d(TAG, "Category threshold crossed: ${category.name} $percentage% (tier=$tier)")

        // 1. Local notification (system tray)
        if (isOver) {
            NotificationHelper.showEventCategoryOver(
                context = context,
                eventName = eventName,
                categoryName = category.name,
                percentage = percentage,
                spent = category.spentAmount,
                budget = category.effectiveBudget
            )
        } else {
            NotificationHelper.showEventCategoryWarning(
                context = context,
                eventName = eventName,
                categoryName = category.name,
                percentage = percentage,
                spent = category.spentAmount,
                budget = category.effectiveBudget
            )
        }

        // 2. In-app notification + push
        sendInAppNotification(
            familyId = familyId,
            actorUserId = actorUserId,
            title = if (isOver) "Budget ${category.name} Terlampaui"
            else "Peringatan Budget: ${category.name}",
            message = if (isOver) {
                "Kategori ${category.name} di event $eventName melampaui budget! " +
                        "Terpakai ${CurrencyFormatter.formatRupiah(category.spentAmount)} " +
                        "dari ${CurrencyFormatter.formatRupiah(category.effectiveBudget)}"
            } else {
                "Kategori ${category.name} di event $eventName mencapai $percentage% " +
                        "(${CurrencyFormatter.formatRupiah(category.spentAmount)} dari " +
                        "${CurrencyFormatter.formatRupiah(category.effectiveBudget)})"
            },
            type = if (isOver) "event_category_over" else "event_category_warning",
            extraData = mapOf(
                "eventId" to eventId,
                "categoryId" to categoryId,
                "categoryName" to category.name,
                "eventName" to eventName,
                "spent" to category.spentAmount.toString(),
                "budget" to category.effectiveBudget.toString(),
                "percentage" to percentage.toString()
            )
        )

        // 3. Update tier di Firestore
        eventRepository.updateCategoryNotifiedPercentage(eventId, categoryId, tier)
    }

    private suspend fun checkEvent(
        event: com.app.cashflowfamily.data.model.Event,
        actorUserId: String
    ) {
        if (event.totalBudget <= 0) return

        val percentage = event.budgetPercentage.toInt()
        val tier = computeTier(percentage)

        if (tier == 0 || tier <= event.lastNotifiedPercentage) return

        val isOver = tier >= 100

        Log.d(TAG, "Event threshold crossed: ${event.name} $percentage% (tier=$tier)")

        // 1. Local notification
        if (isOver) {
            NotificationHelper.showEventOver(
                context = context,
                eventName = event.name,
                percentage = percentage,
                spent = event.spentAmount,
                budget = event.totalBudget
            )
        } else {
            NotificationHelper.showEventWarning(
                context = context,
                eventName = event.name,
                percentage = percentage,
                spent = event.spentAmount,
                budget = event.totalBudget
            )
        }

        // 2. In-app + push
        sendInAppNotification(
            familyId = event.familyId,
            actorUserId = actorUserId,
            title = if (isOver) "Event ${event.name} Over Budget"
            else "Peringatan Budget: ${event.name}",
            message = if (isOver) {
                "Total pengeluaran event ${event.name} melampaui budget! " +
                        "Terpakai ${CurrencyFormatter.formatRupiah(event.spentAmount)} " +
                        "dari ${CurrencyFormatter.formatRupiah(event.totalBudget)}"
            } else {
                "Total pengeluaran event ${event.name} mencapai $percentage% " +
                        "(${CurrencyFormatter.formatRupiah(event.spentAmount)} dari " +
                        "${CurrencyFormatter.formatRupiah(event.totalBudget)})"
            },
            type = if (isOver) "event_over" else "event_warning",
            extraData = mapOf(
                "eventId" to event.eventId,
                "eventName" to event.name,
                "spent" to event.spentAmount.toString(),
                "budget" to event.totalBudget.toString(),
                "percentage" to percentage.toString()
            )
        )

        // 3. Update tier di Firestore
        eventRepository.updateEventNotifiedPercentage(event.eventId, tier)
    }

    private suspend fun sendInAppNotification(
        familyId: String,
        actorUserId: String,
        title: String,
        message: String,
        type: String,
        extraData: Map<String, String>
    ) {
        val family = familyRepository.getFamilyById(familyId).getOrNull() ?: return
        if (family.members.isEmpty()) return

        // Broadcast ke semua member family (termasuk actor sendiri,
        // karena dia mungkin belum lihat layarnya)
        val notifications = family.members.map { memberId ->
            Notification(
                familyId = familyId,
                userId = memberId,
                type = type,
                title = title,
                message = message,
                data = extraData
            )
        }

        notificationRepository.addNotifications(notifications)
            .onSuccess { savedNotifications ->
                val recipients = savedNotifications.associate { it.userId to it.notificationId }
                PushNotifier.notify(
                    recipients = recipients,
                    actorUserId = actorUserId,
                    type = type,
                    title = title,
                    message = message
                )
            }
            .onFailure { error ->
                Log.e(TAG, "Failed to send in-app notifications", error)
            }
    }

    private fun computeTier(percentage: Int): Int {
        return when {
            percentage >= 100 -> 100
            percentage >= 80 -> 80
            else -> 0
        }
    }
}