package com.app.cashflowfamily.data.repository

import android.util.Log
import com.app.cashflowfamily.data.model.RecurringTransaction
import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.utils.RecurringCalculator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val transactionRepository: TransactionRepository
) {

    // Ambil semua recurring transaction keluarga
    suspend fun getRecurringTransactions(familyId: String): Result<List<RecurringTransaction>> {
        return try {
            val snapshot = firestore.collection("recurring_transactions")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("active", true)
                .get()
                .await()

            val recurrings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RecurringTransaction::class.java)?.copy(recurringId = doc.id)
            }

            Log.d("RecurringRepo", "Found ${recurrings.size} recurring")
            Result.success(recurrings)
        } catch (e: Exception) {
            Log.e("RecurringRepo", "Error fetching recurring", e)
            Result.failure(e)
        }
    }

    // Ambil recurring by ID
    suspend fun getRecurringById(recurringId: String): Result<RecurringTransaction> {
        return try {
            val doc = firestore.collection("recurring_transactions")
                .document(recurringId)
                .get()
                .await()

            val recurring = doc.toObject(RecurringTransaction::class.java)
                ?.copy(recurringId = doc.id)
                ?: return Result.failure(Exception("Recurring tidak ditemukan"))

            Result.success(recurring)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Tambah recurring baru
    suspend fun addRecurring(recurring: RecurringTransaction): Result<RecurringTransaction> {
        return try {
            val ref = firestore.collection("recurring_transactions").document()

            // Hitung nextDueDate pertama
            val firstDueDate = RecurringCalculator.calculateFirstDueDate(
                frequency = recurring.frequency,
                startDate = recurring.startDate,
                dayOfMonth = recurring.dayOfMonth,
                dayOfWeek = recurring.dayOfWeek
            )

            val newRecurring = recurring.copy(
                recurringId = ref.id,
                nextDueDate = firstDueDate
            )

            ref.set(newRecurring).await()

            Log.d("RecurringRepo", "Recurring created: ${ref.id}")
            Result.success(newRecurring)
        } catch (e: Exception) {
            Log.e("RecurringRepo", "Error adding recurring", e)
            Result.failure(e)
        }
    }

    // Update recurring
    suspend fun updateRecurring(recurring: RecurringTransaction): Result<RecurringTransaction> {
        return try {
            firestore.collection("recurring_transactions")
                .document(recurring.recurringId)
                .set(recurring)
                .await()

            Result.success(recurring)
        } catch (e: Exception) {
            Log.e("RecurringRepo", "Error updating recurring", e)
            Result.failure(e)
        }
    }

    // Delete recurring
    suspend fun deleteRecurring(recurringId: String): Result<Unit> {
        return try {
            firestore.collection("recurring_transactions")
                .document(recurringId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Toggle active/inactive
    suspend fun toggleActive(recurringId: String, isActive: Boolean): Result<Unit> {
        return try {
            firestore.collection("recurring_transactions")
                .document(recurringId)
                .update("active", isActive)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cek semua recurring dan generate transaksi kalau sudah due.
     * Return jumlah transaksi yang berhasil di-generate.
     */
    suspend fun processDueRecurrings(familyId: String): Result<Int> {
        return try {
            val recurringsResult = getRecurringTransactions(familyId)
            if (recurringsResult.isFailure) {
                return Result.failure(
                    recurringsResult.exceptionOrNull() ?: Exception("Unknown error")
                )
            }

            val recurrings = recurringsResult.getOrNull() ?: emptyList()
            var generatedCount = 0

            recurrings.forEach { recurring ->
                // Cek expired
                if (RecurringCalculator.isExpired(recurring.endDate)) {
                    // Set inactive
                    toggleActive(recurring.recurringId, false)
                    return@forEach
                }

                // Process semua due dates yang terlewat
                var currentDueDate = recurring.nextDueDate
                while (RecurringCalculator.isDue(currentDueDate) &&
                    !RecurringCalculator.isExpired(recurring.endDate)) {

                    // Cek endDate spesifik untuk due date ini
                    if (recurring.endDate != null && currentDueDate > recurring.endDate) {
                        break
                    }

                    // Generate transaksi
                    val generated = generateRecurringTransactionIfNeeded(
                        recurring = recurring,
                        dueDate = currentDueDate
                    )

                    if (generated) {
                        generatedCount++
                    }

                    // Hitung due date berikutnya
                    currentDueDate = RecurringCalculator.calculateNextDueDate(
                        frequency = recurring.frequency,
                        currentDate = currentDueDate,
                        dayOfMonth = recurring.dayOfMonth,
                        dayOfWeek = recurring.dayOfWeek
                    )
                }

                // Update recurring dengan nextDueDate & lastGenerated baru
                if (currentDueDate != recurring.nextDueDate) {
                    val updated = recurring.copy(
                        nextDueDate = currentDueDate,
                        lastGeneratedDate = System.currentTimeMillis()
                    )
                    updateRecurring(updated)
                }
            }

            Log.d("RecurringRepo", "Total generated: $generatedCount")
            Result.success(generatedCount)
        } catch (e: Exception) {
            Log.e("RecurringRepo", "Error processing due recurrings", e)
            Result.failure(e)
        }
    }

    private suspend fun generateRecurringTransactionIfNeeded(
        recurring: RecurringTransaction,
        dueDate: Long
    ): Boolean {
        return try {
            val generatedTransactionId = "recurring_${recurring.recurringId}_$dueDate"

            val transactionRef = firestore.collection("transactions")
                .document(generatedTransactionId)

            val existingDoc = transactionRef.get().await()

            if (existingDoc.exists()) {
                Log.d(
                    "RecurringRepo",
                    "Transaction already generated for ${recurring.name} at $dueDate"
                )
                return false
            }

            val transaction = Transaction(
                transactionId = generatedTransactionId,
                familyId = recurring.familyId,
                userId = recurring.userId,
                userName = recurring.userName,
                type = recurring.type,
                amount = recurring.amount,
                category = recurring.category,
                description = "${recurring.name} (Otomatis)",
                date = dueDate,
                createdAt = System.currentTimeMillis(),
                recurringId = recurring.recurringId,
                recurringGeneratedFor = dueDate,
                recurringGenerated = true
            )

            transactionRef.set(transaction).await()

            Log.d(
                "RecurringRepo",
                "Generated recurring transaction: $generatedTransactionId"
            )

            true
        } catch (e: Exception) {
            Log.e("RecurringRepo", "Error generating recurring transaction", e)
            false
        }
    }
}