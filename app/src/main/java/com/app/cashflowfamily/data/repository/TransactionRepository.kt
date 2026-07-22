package com.app.cashflowfamily.data.repository

import com.app.cashflowfamily.data.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class TransactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // Ambil semua transaksi keluarga
    suspend fun getTransactions(familyId: String): Result<List<Transaction>> {
        return try {
            Log.d("TransactionRepo", "Fetching transactions for familyId: $familyId")

            val snapshot = firestore.collection("transactions")
                .whereEqualTo("familyId", familyId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)?.copy(transactionId = doc.id)
            }

            Log.d("TransactionRepo", "Found ${transactions.size} transactions")

            Result.success(transactions)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching transactions", e)
            Result.failure(e)
        }
    }

    // Ambil transaksi terbaru (limit)
    suspend fun getRecentTransactions(familyId: String, limit: Long = 5): Result<List<Transaction>> {
        return try {
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("familyId", familyId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)?.copy(transactionId = doc.id)
            }

            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Simpan transaksi baru
    suspend fun addTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            val transactionRef = firestore.collection("transactions").document()

            val newTransaction = transaction.copy(
                transactionId = transactionRef.id,
                createdAt = System.currentTimeMillis()
            )

            Log.d("TransactionRepo", "Saving transaction: $newTransaction")

            transactionRef.set(newTransaction).await()

            Log.d("TransactionRepo", "Transaction saved with ID: ${transactionRef.id}")

            Result.success(newTransaction)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error saving transaction", e)
            Result.failure(e)
        }
    }

    // Ambil detail transaksi berdasarkan ID
    suspend fun getTransactionById(transactionId: String): Result<Transaction> {
        return try {
            Log.d("TransactionRepo", "Fetching transaction: $transactionId")

            val doc = firestore.collection("transactions")
                .document(transactionId)
                .get()
                .await()

            val transaction = doc.toObject(Transaction::class.java)?.copy(transactionId = doc.id)
                ?: return Result.failure(Exception("Transaksi tidak ditemukan"))

            Result.success(transaction)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching transaction", e)
            Result.failure(e)
        }
    }

    // Update transaksi
    suspend fun updateTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            Log.d("TransactionRepo", "Updating transaction: ${transaction.transactionId}")

            firestore.collection("transactions")
                .document(transaction.transactionId)
                .set(transaction)
                .await()

            Log.d("TransactionRepo", "Transaction updated successfully")

            Result.success(transaction)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error updating transaction", e)
            Result.failure(e)
        }
    }

    // Delete transaksi
    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            Log.d("TransactionRepo", "Deleting transaction: $transactionId")

            firestore.collection("transactions")
                .document(transactionId)
                .delete()
                .await()

            Log.d("TransactionRepo", "Transaction deleted successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error deleting transaction", e)
            Result.failure(e)
        }
    }

    // Hitung transaksi per user bulan ini
    suspend fun countTransactionsByUser(
        familyId: String,
        startOfMonth: Long,
        endOfMonth: Long
    ): Result<Map<String, Int>> {
        return try {
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("familyId", familyId)
                .whereGreaterThanOrEqualTo("date", startOfMonth)
                .whereLessThanOrEqualTo("date", endOfMonth)
                .get()
                .await()

            val countMap = mutableMapOf<String, Int>()

            snapshot.documents.forEach { doc ->
                val userId = doc.getString("userId") ?: return@forEach
                countMap[userId] = (countMap[userId] ?: 0) + 1
            }

            Result.success(countMap)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error counting transactions", e)
            Result.failure(e)
        }
    }

    // Real-time listener untuk transaksi baru
    fun observeNewTransactions(
        familyId: String,
        currentUserId: String,
        startTimestamp: Long,
        onNewTransaction: (Transaction) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        Log.d("TransactionRepo", "Starting listener for familyId: $familyId")

        return firestore.collection("transactions")
            .whereEqualTo("familyId", familyId)
            .whereGreaterThan("createdAt", startTimestamp)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TransactionRepo", "Listener error", error)
                    return@addSnapshotListener
                }

                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val transaction = change.document.toObject(Transaction::class.java)
                            .copy(transactionId = change.document.id)

                        // Skip transaksi dari user sendiri
                        if (transaction.userId != currentUserId) {
                            Log.d("TransactionRepo", "New transaction from: ${transaction.userName}")
                            onNewTransaction(transaction)
                        }
                    }
                }
            }
    }
}