package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.data.model.User
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.TransactionRepository
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailUiState(
    val isLoading: Boolean = false,
    val transaction: Transaction? = null,
    val currentUser: User? = null,
    val canEdit: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository,
    private val budgetThresholdNotifier: com.app.cashflowfamily.utils.BudgetThresholdNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<Resource<Transaction>>(Resource.Idle)
    val updateState: StateFlow<Resource<Transaction>> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val deleteState: StateFlow<Resource<Unit>> = _deleteState.asStateFlow()

    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Ambil data user dulu untuk cek permission
            val currentAuthUser = authRepository.getCurrentUser()
            if (currentAuthUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "User tidak terautentikasi"
                )
                return@launch
            }

            authRepository.getUserData(currentAuthUser.uid)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(currentUser = user)

                    // Ambil transaksi
                    transactionRepository.getTransactionById(transactionId)
                        .onSuccess { transaction ->
                            // Cek permission:
                            // - User yang membuat transaksi bisa edit/delete
                            // - Admin keluarga juga bisa edit/delete semua transaksi
                            val canEdit = user.userId == transaction.userId ||
                                    user.role == "admin"

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                transaction = transaction,
                                canEdit = canEdit
                            )
                        }
                        .onFailure { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun updateTransaction(
        type: String,
        amount: Double,
        category: String,
        description: String,
        date: Long
    ) {
        val currentTransaction = _uiState.value.transaction ?: return

        viewModelScope.launch {
            _updateState.value = Resource.Loading

            val updated = currentTransaction.copy(
                type = type,
                amount = amount,
                category = category,
                description = description,
                date = date
            )

            transactionRepository.updateTransaction(updated)
                .onSuccess { transaction ->
                    _updateState.value = Resource.Success(transaction)
                    _uiState.value = _uiState.value.copy(transaction = transaction)

                    // ===== CEK BUDGET =====
                    // Perubahan jumlah/kategori/tanggal transaksi bisa membuat
                    // budget kategori (lama atau baru) melewati ambang batas.
                    viewModelScope.launch {
                        budgetThresholdNotifier.checkAndNotify(
                            familyId = transaction.familyId,
                            category = transaction.category,
                            type = transaction.type,
                            date = transaction.date
                        )
                    }
                }
                .onFailure { error ->
                    _updateState.value = Resource.Error(
                        error.message ?: "Gagal update transaksi"
                    )
                }
        }
    }

    fun deleteTransaction() {
        val currentTransaction = _uiState.value.transaction ?: return

        viewModelScope.launch {
            _deleteState.value = Resource.Loading

            transactionRepository.deleteTransaction(currentTransaction.transactionId)
                .onSuccess {
                    _deleteState.value = Resource.Success(Unit)
                }
                .onFailure { error ->
                    _deleteState.value = Resource.Error(
                        error.message ?: "Gagal menghapus transaksi"
                    )
                }
        }
    }

    fun resetUpdateState() {
        _updateState.value = Resource.Idle
    }

    fun resetDeleteState() {
        _deleteState.value = Resource.Idle
    }
}