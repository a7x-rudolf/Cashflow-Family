@file:Suppress("AddExplicitTargetToParameterAnnotation")

package com.app.cashflowfamily.utils

import android.content.Context
import android.util.Log
import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.data.preferences.NotificationPreferences
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.TransactionRepository
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HANYA untuk menampilkan system notification lokal secara instan di device
 * member lain SAAT app mereka sedang foreground (real-time UX tambahan).
 *
 * TIDAK BOLEH menulis ke Firestore collection `notifications` dan TIDAK BOLEH
 * memanggil PushNotifier di sini -- itu semua sudah ditangani sepenuhnya oleh
 * AddTransactionViewModel.sendNotifications() di device pembuat transaksi.
 * Kalau ditambahkan lagi di sini, notifikasi & push akan terkirim DOBEL.
 */
@Singleton
class FamilyActivityListener @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository,
    private val notificationPreferences: NotificationPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listener: ListenerRegistration? = null
    private var listenerStartTime: Long = 0L

    fun startListening() {
        stopListening()

        val currentUser = authRepository.getCurrentUser() ?: return

        scope.launch {
            try {
                authRepository.getUserData(currentUser.uid)
                    .onSuccess { user ->
                        if (user.familyId.isEmpty()) {
                            Log.d("FamilyListener", "User has no family, skip listener")
                            return@onSuccess
                        }

                        listenerStartTime = System.currentTimeMillis()
                        Log.d("FamilyListener", "Starting listener at $listenerStartTime")

                        listener = transactionRepository.observeNewTransactions(
                            familyId = user.familyId,
                            currentUserId = user.userId,
                            startTimestamp = listenerStartTime
                        ) { newTransaction ->
                            handleNewTransaction(newTransaction)
                        }
                    }
                    .onFailure { error ->
                        Log.e("FamilyListener", "Failed to get user data", error)
                    }
            } catch (e: Exception) {
                Log.e("FamilyListener", "Error starting listener", e)
            }
        }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
        Log.d("FamilyListener", "Listener stopped")
    }

    private fun handleNewTransaction(transaction: Transaction) {
        scope.launch {
            try {
                val isEnabled = notificationPreferences.isFamilyActivityEnabled.first()

                if (!isEnabled) {
                    Log.d("FamilyListener", "Family activity notification disabled")
                    return@launch
                }

                Log.d("FamilyListener", "Showing local notification for: ${transaction.userName}")

                // ===== SYSTEM NOTIFICATION LOKAL SAJA (tidak menulis Firestore, tidak push) =====
                NotificationHelper.showFamilyTransactionNotification(
                    context = context,
                    userName = transaction.userName,
                    transactionType = transaction.type,
                    amount = transaction.amount,
                    category = transaction.category
                )

            } catch (e: Exception) {
                Log.e("FamilyListener", "Error handling new transaction", e)
            }
        }
    }
}