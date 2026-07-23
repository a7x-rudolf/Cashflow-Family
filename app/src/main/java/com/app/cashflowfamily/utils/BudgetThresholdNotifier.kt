package com.app.cashflowfamily.utils

import android.content.Context
import android.util.Log
import com.app.cashflowfamily.data.model.Notification
import com.app.cashflowfamily.data.preferences.NotificationPreferences
import com.app.cashflowfamily.data.repository.BudgetRepository
import com.app.cashflowfamily.data.repository.FamilyRepository
import com.app.cashflowfamily.data.repository.NotificationRepository
import com.app.cashflowfamily.data.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper terpusat untuk mengecek apakah pengeluaran (baru / diedit / hasil
 * recurring) membuat budget kategori tertentu melewati ambang batas
 * (80% = warning, 100% = over), lalu mengirim notifikasi (system + in-app)
 * ke semua anggota keluarga.
 *
 * Dipakai dari 3 tempat:
 *  - AddTransactionViewModel        -> transaksi baru dari form
 *  - TransactionDetailViewModel     -> transaksi yang diedit
 *  - RecurringRepository            -> transaksi hasil auto-generate recurring
 *
 * Supaya tidak spam notifikasi berulang untuk kategori & bulan yang sama,
 * tier threshold yang sudah dikirim dicatat di Budget.lastNotifiedPercentage.
 * Field ini otomatis "reset" tiap bulan karena Budget adalah dokumen baru
 * per month/year (lihat BudgetRepository.addBudget).
 */
@Singleton
class BudgetThresholdNotifier @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val familyRepository: FamilyRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationPreferences: NotificationPreferences,
    @ApplicationContext private val context: Context
) {

    /**
     * Panggil ini setiap kali sebuah transaksi expense dibuat, diedit,
     * atau di-generate otomatis dari recurring.
     */
    suspend fun checkAndNotify(
        familyId: String,
        category: String,
        type: String,
        date: Long
    ) {
        // Cuma pengeluaran yang relevan untuk budget
        if (type != "expense" || familyId.isBlank() || category.isBlank()) return

        try {
            val isEnabled = notificationPreferences.isBudgetWarningEnabled.first()
            if (!isEnabled) return

            val month = DateFormatter.getMonth(date)
            val year = DateFormatter.getYear(date)

            val budget = budgetRepository.getBudgets(familyId, month, year)
                .getOrNull()
                ?.find { it.category == category }
                ?: return

            if (budget.amount <= 0) return

            val transactions = transactionRepository.getTransactions(familyId)
                .getOrNull() ?: return

            val spent = transactions
                .filter {
                    it.type == "expense" &&
                        it.category == category &&
                        DateFormatter.isSameMonth(it.date, date)
                }
                .sumOf { it.amount }

            val percentage = ((spent / budget.amount) * 100).toInt()

            val tier = when {
                percentage >= 100 -> 100
                percentage >= 80 -> 80
                else -> 0
            }

            // Belum lewat threshold manapun, atau tier ini (atau yang lebih
            // tinggi) sudah pernah dikirim bulan ini -> jangan kirim lagi.
            if (tier == 0 || tier <= budget.lastNotifiedPercentage) return

            val isOver = tier >= 100

            Log.d(
                "BudgetThresholdNotifier",
                "Threshold crossed: $category $percentage% (tier=$tier, familyId=$familyId)"
            )

            if (isOver) {
                NotificationHelper.showBudgetOver(
                    context = context,
                    category = category,
                    percentage = percentage,
                    spent = spent,
                    budget = budget.amount
                )
            } else {
                NotificationHelper.showBudgetWarning(
                    context = context,
                    category = category,
                    percentage = percentage,
                    spent = spent,
                    budget = budget.amount
                )
            }

            sendInAppNotification(
                familyId = familyId,
                category = category,
                percentage = percentage,
                spent = spent,
                budgetAmount = budget.amount,
                isOver = isOver
            )

            // Catat tier ini supaya tidak dikirim ulang bulan ini.
            budgetRepository.updateNotifiedPercentage(budget.budgetId, tier)

        } catch (e: Exception) {
            // Notifikasi budget tidak boleh sampai menggagalkan alur simpan
            // transaksi utama, jadi errornya cukup dicatat saja.
            Log.e("BudgetThresholdNotifier", "Error checking budget threshold", e)
        }
    }

    private suspend fun sendInAppNotification(
        familyId: String,
        category: String,
        percentage: Int,
        spent: Double,
        budgetAmount: Double,
        isOver: Boolean
    ) {
        val family = familyRepository.getFamilyById(familyId).getOrNull() ?: return
        if (family.members.isEmpty()) return

        val title = if (isOver) {
            "Budget Terlampaui: $category"
        } else {
            "Peringatan Budget: $category"
        }

        val message = if (isOver) {
            "Budget $category terlampaui! Terpakai ${CurrencyFormatter.formatRupiah(spent)} dari ${CurrencyFormatter.formatRupiah(budgetAmount)}"
        } else {
            "Budget $category sudah mencapai $percentage% (${CurrencyFormatter.formatRupiah(spent)} dari ${CurrencyFormatter.formatRupiah(budgetAmount)})"
        }

        val notifications = family.members.map { memberId ->
            Notification(
                familyId = familyId,
                userId = memberId,
                type = if (isOver) "budget_over" else "budget_warning",
                title = title,
                message = message,
                data = mapOf(
                    "category" to category,
                    "spent" to spent.toString(),
                    "budget" to budgetAmount.toString(),
                    "percentage" to percentage.toString()
                )
            )
        }

        notificationRepository.addNotifications(notifications)
            .onFailure { error ->
                Log.e("BudgetThresholdNotifier", "Failed to send in-app notifications", error)
            }
    }
}
