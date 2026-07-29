package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.EventCategory
import com.app.cashflowfamily.data.model.EventTransaction
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.EventRepository
import com.app.cashflowfamily.utils.EventThresholdNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventTransactionUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val transactions: List<EventTransaction> = emptyList(),
    val categories: List<EventCategory> = emptyList(),
    val editingTransaction: EventTransaction? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class EventTransactionViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
    private val thresholdNotifier: EventThresholdNotifier  // ← BARU
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventTransactionUiState())
    val uiState: StateFlow<EventTransactionUiState> = _uiState.asStateFlow()

    private var familyId: String = ""
    private var userId: String = ""
    private val userInfoReady = CompletableDeferred<Boolean>()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser()
            if (firebaseUser == null) {
                userInfoReady.complete(false)
                return@launch
            }
            authRepository.getUserData(firebaseUser.uid)
                .onSuccess { user ->
                    familyId = user.familyId
                    userId = user.userId
                    userInfoReady.complete(familyId.isNotBlank())
                }
                .onFailure { e ->
                    userInfoReady.complete(false)
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    private suspend fun ensureUserReady(): Boolean = userInfoReady.await()

    fun loadTransactions(eventId: String, categoryId: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val txResult = eventRepository.getTransactions(eventId, categoryId)
            val catResult = eventRepository.getCategories(eventId)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    transactions = txResult.getOrElse { emptyList() },
                    categories = catResult.getOrElse { emptyList() },
                    error = txResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun loadTransactionForEdit(eventId: String, transactionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val catResult = eventRepository.getCategories(eventId)
            val txResult = eventRepository.getTransactionById(eventId, transactionId)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    categories = catResult.getOrElse { emptyList() },
                    editingTransaction = txResult.getOrNull(),
                    error = txResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun addTransaction(
        eventId: String, categoryId: String, name: String,
        amount: Double, transactionDate: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val ready = ensureUserReady()
            if (!ready) {
                _uiState.update { it.copy(error = "Data pengguna belum siap") }
                return@launch
            }
            if (name.isBlank()) {
                _uiState.update { it.copy(error = "Deskripsi harus diisi") }
                return@launch
            }
            if (categoryId.isBlank()) {
                _uiState.update { it.copy(error = "Kategori harus dipilih") }
                return@launch
            }
            if (amount <= 0) {
                _uiState.update { it.copy(error = "Jumlah harus lebih dari 0") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true) }
            val transaction = EventTransaction(
                eventId = eventId, categoryId = categoryId, familyId = familyId,
                createdBy = userId, name = name.trim(), amount = amount,
                transactionDate = transactionDate
            )
            eventRepository.addTransaction(transaction)
                .onSuccess {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "Berhasil disimpan")
                    }
                    onSuccess()

                    // === TRIGGER NOTIFIKASI === (fire and forget, tidak block UI)
                    launch {
                        thresholdNotifier.checkAndNotify(
                            eventId = eventId,
                            categoryId = categoryId,
                            actorUserId = userId
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun updateTransaction(
        oldAmount: Double,
        transaction: EventTransaction,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (transaction.name.isBlank()) {
                _uiState.update { it.copy(error = "Deskripsi harus diisi") }
                return@launch
            }
            if (transaction.categoryId.isBlank()) {
                _uiState.update { it.copy(error = "Kategori harus dipilih") }
                return@launch
            }
            if (transaction.amount <= 0) {
                _uiState.update { it.copy(error = "Jumlah harus lebih dari 0") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true) }
            eventRepository.updateTransaction(oldAmount, transaction)
                .onSuccess {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "Berhasil diperbarui")
                    }
                    onSuccess()

                    // === TRIGGER NOTIFIKASI ===
                    launch {
                        thresholdNotifier.checkAndNotify(
                            eventId = transaction.eventId,
                            categoryId = transaction.categoryId,
                            actorUserId = userId
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun deleteTransaction(transaction: EventTransaction) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            eventRepository.deleteTransaction(transaction)
                .onSuccess {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "Berhasil dihapus")
                    }
                    loadTransactions(transaction.eventId)

                    // === TRIGGER NOTIFIKASI ===
                    // (delete tidak akan trigger warning baru, tapi bisa reset tier
                    //  yang sudah di-handle di repository)
                    launch {
                        thresholdNotifier.checkAndNotify(
                            eventId = transaction.eventId,
                            categoryId = transaction.categoryId,
                            actorUserId = userId
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }
    fun clearEditingTransaction() = _uiState.update { it.copy(editingTransaction = null) }
}