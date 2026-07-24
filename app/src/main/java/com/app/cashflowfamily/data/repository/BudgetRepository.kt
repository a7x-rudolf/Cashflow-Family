package com.app.cashflowfamily.data.repository

import android.util.Log
import com.app.cashflowfamily.data.model.Budget
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Ambil semua budget untuk bulan tertentu
    suspend fun getBudgets(
        familyId: String,
        month: Int,
        year: Int
    ): Result<List<Budget>> {
        return try {
            Log.d("BudgetRepo", "Fetching budgets for $familyId, $month/$year")

            val snapshot = firestore.collection("budgets")
                .whereEqualTo("familyId", familyId)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .await()

            val budgets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Budget::class.java)?.copy(budgetId = doc.id)
            }

            Log.d("BudgetRepo", "Found ${budgets.size} budgets")

            Result.success(budgets)
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Error fetching budgets", e)
            Result.failure(e)
        }
    }

    // Tambah budget baru
    suspend fun addBudget(budget: Budget): Result<Budget> {
        return try {
            // Cek apakah budget kategori & bulan yang sama sudah ada
            val existing = firestore.collection("budgets")
                .whereEqualTo("familyId", budget.familyId)
                .whereEqualTo("category", budget.category)
                .whereEqualTo("month", budget.month)
                .whereEqualTo("year", budget.year)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.failure(
                    Exception("Budget untuk kategori ini sudah ada di bulan ini")
                )
            }

            val budgetRef = firestore.collection("budgets").document()
            val newBudget = budget.copy(
                budgetId = budgetRef.id,
                createdAt = System.currentTimeMillis()
            )

            budgetRef.set(newBudget).await()
            Log.d("BudgetRepo", "Budget saved: ${budgetRef.id}")

            Result.success(newBudget)
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Error adding budget", e)
            Result.failure(e)
        }
    }

    // Update budget
    suspend fun updateBudget(budget: Budget): Result<Budget> {
        return try {
            firestore.collection("budgets")
                .document(budget.budgetId)
                .set(budget)
                .await()

            Result.success(budget)
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Error updating budget", e)
            Result.failure(e)
        }
    }

    // Catat tier threshold (80/100) terakhir yang sudah dikirim notifikasinya,
    // supaya BudgetThresholdNotifier tidak kirim notifikasi berulang untuk
    // tier yang sama di bulan yang sama.
    suspend fun updateNotifiedPercentage(budgetId: String, tier: Int): Result<Unit> {
        return try {
            firestore.collection("budgets")
                .document(budgetId)
                .update("lastNotifiedPercentage", tier)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Error updating notified percentage", e)
            Result.failure(e)
        }
    }

    // Delete budget
    suspend fun deleteBudget(budgetId: String): Result<Unit> {
        return try {
            firestore.collection("budgets")
                .document(budgetId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Error deleting budget", e)
            Result.failure(e)
        }
    }
}