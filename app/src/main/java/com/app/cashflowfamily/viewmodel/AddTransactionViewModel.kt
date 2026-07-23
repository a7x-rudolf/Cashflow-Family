@file:Suppress("AddExplicitTargetToParameterAnnotation")

package com.app.cashflowfamily.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.FamilyRepository
import com.app.cashflowfamily.data.repository.NotificationRepository
import com.app.cashflowfamily.data.repository.TransactionRepository
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.utils.NotificationHelper
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val familyRepository: FamilyRepository,
    private val budgetThresholdNotifier: com.app.cashflowfamily.utils.BudgetThresholdNotifier,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _saveState = MutableStateFlow<Resource<Transaction>>(Resource.Idle)
    val saveState: StateFlow<Resource<Transaction>> = _saveState.asStateFlow()

    fun saveTransaction(
        type: String,
        amount: Double,
        category: String,
        description: String,
        date: Long
    ) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _saveState.value = Resource.Error("User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _saveState.value = Resource.Loading

            authRepository.getUserData(currentUser.uid)
                .onSuccess { user ->
                    if (user.familyId.isEmpty()) {
                        _saveState.value = Resource.Error("Anda belum terdaftar di keluarga")
                        return@onSuccess
                    }

                    val transaction = Transaction(
                        familyId = user.familyId,
                        userId = user.userId,
                        userName = user.name,
                        type = type,
                        amount = amount,
                        category = category,
                        description = description,
                        date = date
                    )

                    transactionRepository.addTransaction(transaction)
                        .onSuccess { savedTransaction ->
                            _saveState.value = Resource.Success(savedTransaction)

                            // ===== KIRIM NOTIFIKASI =====
                            sendNotifications(savedTransaction, user)

                            // ===== CEK BUDGET =====
                            viewModelScope.launch {
                                budgetThresholdNotifier.checkAndNotify(
                                    familyId = user.familyId,
                                    category = category,
                                    type = type,
                                    date = date
                                )
                            }
                        }
                        .onFailure { error ->
                            _saveState.value = Resource.Error(
                                error.message ?: "Gagal menyimpan transaksi"
                            )
                        }
                }
                .onFailure { error ->
                    _saveState.value = Resource.Error(
                        error.message ?: "Gagal mengambil data user"
                    )
                }
        }
    }

    /**
     * Kirim notifikasi ke semua member keluarga (In-App) + System notification untuk user sendiri
     */
    private fun sendNotifications(
        transaction: Transaction,
        user: com.app.cashflowfamily.data.model.User
    ) {
        viewModelScope.launch {
            try {
                // ===== 1. IN-APP NOTIFICATION UNTUK SEMUA MEMBER KELUARGA =====
                val family = familyRepository.getFamilyById(user.familyId).getOrNull()

                if (family != null && family.members.isNotEmpty()) {
                    val notifications = family.members.map { memberId ->
                        com.app.cashflowfamily.data.model.Notification(
                            familyId = user.familyId,
                            userId = memberId,
                            type = "family_activity",
                            title = "Transaksi Baru oleh ${user.name}",
                            message = "${user.name} mencatat ${if (transaction.type == "income") "pemasukan" else "pengeluaran"} ${CurrencyFormatter.formatRupiah(transaction.amount)} - ${transaction.category}",
                            data = mapOf(
                                "transactionId" to transaction.transactionId,
                                "userId" to user.userId,
                                "userName" to user.name,
                                "type" to transaction.type,
                                "amount" to transaction.amount.toString()
                            )
                        )
                    }

                    notificationRepository.addNotifications(notifications)
                        .onSuccess {
                            Log.d("AddTransactionVM", "Broadcast notifications sent to ${notifications.size} members")
                        }
                        .onFailure { error ->
                            Log.e("AddTransactionVM", "Failed to broadcast notifications", error)
                        }
                }

                // ===== 2. SYSTEM NOTIFICATION (Status Bar) =====
                // Hanya untuk user yang login (tidak broadcast ke semua)
                NotificationHelper.showFamilyTransactionNotification(
                    context = context,
                    userName = user.name,
                    transactionType = transaction.type,
                    amount = transaction.amount,
                    category = transaction.category
                )
                Log.d("AddTransactionVM", "System notification sent")

            } catch (e: Exception) {
                Log.e("AddTransactionVM", "Error sending notification", e)
            }
        }
    }

    fun resetState() {
        _saveState.value = Resource.Idle
    }

}