package com.app.cashflowfamily.data.repository

import android.util.Log
import com.app.cashflowfamily.viewmodel.BackupData
import com.app.cashflowfamily.data.model.Budget
import com.app.cashflowfamily.data.model.Family
import com.app.cashflowfamily.data.model.RecurringTransaction
import com.app.cashflowfamily.viewmodel.RestoreResult
import com.app.cashflowfamily.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringRepository: RecurringRepository,
    private val familyRepository: FamilyRepository
) {

    /**
     * Gather semua data untuk backup
     */
    suspend fun gatherBackupData(family: Family): Result<BackupData> {
        return try {
            Log.d("BackupRepo", "Gathering backup for family: ${family.familyName}")

            // Ambil semua transaksi
            val transactionsResult = transactionRepository.getTransactions(family.familyId)
            val transactions = transactionsResult.getOrNull() ?: emptyList()

            // Ambil semua budget (untuk semua bulan, jadi kita perlu strategi khusus)
            val budgetsSnapshot = firestore.collection("budgets")
                .whereEqualTo("familyId", family.familyId)
                .get()
                .await()

            val budgets = budgetsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Budget::class.java)?.copy(budgetId = doc.id)
            }

            // Ambil semua recurring transactions
            val recurringsSnapshot = firestore.collection("recurring_transactions")
                .whereEqualTo("familyId", family.familyId)
                .get()
                .await()

            val recurrings = recurringsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(RecurringTransaction::class.java)?.copy(recurringId = doc.id)
            }

            val backupData = BackupData(
                version = "1.0",
                exportDate = System.currentTimeMillis(),
                familyName = family.familyName,
                familyCode = family.familyCode,
                transactionsCount = transactions.size,
                budgetsCount = budgets.size,
                recurringsCount = recurrings.size,
                transactions = transactions,
                budgets = budgets,
                recurringTransactions = recurrings
            )

            Log.d("BackupRepo", "Backup gathered: ${transactions.size} tx, ${budgets.size} budgets, ${recurrings.size} recurring")

            Result.success(backupData)
        } catch (e: Exception) {
            Log.e("BackupRepo", "Error gathering backup", e)
            Result.failure(e)
        }
    }

    /**
     * Restore data dari backup ke family aktif
     */
    suspend fun restoreBackup(
        backupData: BackupData,
        currentUser: User,
        family: Family
    ): Result<RestoreResult> {
        return try {
            Log.d("BackupRepo", "Starting restore process")

            var transactionsImported = 0
            var transactionsSkipped = 0
            var budgetsImported = 0
            var budgetsSkipped = 0
            var recurringsImported = 0
            var recurringsSkipped = 0

            // Load existing data untuk cek duplicate
            val existingTxSnapshot = firestore.collection("transactions")
                .whereEqualTo("familyId", family.familyId)
                .get()
                .await()

            val existingTxIds = existingTxSnapshot.documents.mapNotNull { it.id }.toSet()

            // Import transactions
            backupData.transactions.forEach { transaction ->
                // Skip kalau ID sudah ada (backup ulang scenario)
                if (existingTxIds.contains(transaction.transactionId) && transaction.transactionId.isNotBlank()) {
                    transactionsSkipped++
                    return@forEach
                }

                try {
                    // Adjust data untuk family baru
                    val adjustedTx = transaction.copy(
                        familyId = family.familyId,
                        // Kalau import ke akun berbeda, tetap simpan info user asli
                        userId = transaction.userId.ifBlank { currentUser.userId },
                        userName = transaction.userName.ifBlank { currentUser.name }
                    )

                    val docRef = if (adjustedTx.transactionId.isNotBlank()) {
                        firestore.collection("transactions").document(adjustedTx.transactionId)
                    } else {
                        firestore.collection("transactions").document()
                    }

                    val finalTx = adjustedTx.copy(transactionId = docRef.id)
                    docRef.set(finalTx).await()

                    transactionsImported++
                } catch (e: Exception) {
                    Log.e("BackupRepo", "Error importing transaction: ${transaction.transactionId}", e)
                    transactionsSkipped++
                }
            }

            // Import budgets
            val existingBudgetsSnapshot = firestore.collection("budgets")
                .whereEqualTo("familyId", family.familyId)
                .get()
                .await()

            val existingBudgetKeys = existingBudgetsSnapshot.documents.mapNotNull { doc ->
                val budget = doc.toObject(Budget::class.java)
                if (budget != null) "${budget.category}_${budget.month}_${budget.year}" else null
            }.toSet()

            backupData.budgets.forEach { budget ->
                // Cek duplicate berdasarkan kategori + bulan + tahun
                val key = "${budget.category}_${budget.month}_${budget.year}"
                if (existingBudgetKeys.contains(key)) {
                    budgetsSkipped++
                    return@forEach
                }

                try {
                    val adjustedBudget = budget.copy(
                        familyId = family.familyId,
                        createdBy = budget.createdBy.ifBlank { currentUser.userId }
                    )

                    val docRef = firestore.collection("budgets").document()
                    val finalBudget = adjustedBudget.copy(budgetId = docRef.id)
                    docRef.set(finalBudget).await()

                    budgetsImported++
                } catch (e: Exception) {
                    Log.e("BackupRepo", "Error importing budget", e)
                    budgetsSkipped++
                }
            }

            // Import recurring transactions
            val existingRecurringsSnapshot = firestore.collection("recurring_transactions")
                .whereEqualTo("familyId", family.familyId)
                .get()
                .await()

            val existingRecurringNames = existingRecurringsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(RecurringTransaction::class.java)?.name
            }.toSet()

            backupData.recurringTransactions.forEach { recurring ->
                // Cek duplicate berdasarkan nama
                if (existingRecurringNames.contains(recurring.name)) {
                    recurringsSkipped++
                    return@forEach
                }

                try {
                    val adjustedRecurring = recurring.copy(
                        familyId = family.familyId,
                        userId = recurring.userId.ifBlank { currentUser.userId },
                        userName = recurring.userName.ifBlank { currentUser.name }
                    )

                    val docRef = firestore.collection("recurring_transactions").document()
                    val finalRecurring = adjustedRecurring.copy(recurringId = docRef.id)
                    docRef.set(finalRecurring).await()

                    recurringsImported++
                } catch (e: Exception) {
                    Log.e("BackupRepo", "Error importing recurring", e)
                    recurringsSkipped++
                }
            }

            val result = RestoreResult(
                transactionsImported = transactionsImported,
                transactionsSkipped = transactionsSkipped,
                budgetsImported = budgetsImported,
                budgetsSkipped = budgetsSkipped,
                recurringsImported = recurringsImported,
                recurringsSkipped = recurringsSkipped
            )

            Log.d("BackupRepo", "Restore complete: ${result.totalImported} imported, ${result.totalSkipped} skipped")

            Result.success(result)
        } catch (e: Exception) {
            Log.e("BackupRepo", "Error during restore", e)
            Result.failure(e)
        }
    }
}