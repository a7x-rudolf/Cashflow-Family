package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.RecurringTransaction
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.RecurringRepository
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringUiState(
    val isLoading: Boolean = false,
    val recurrings: List<RecurringTransaction> = emptyList(),
    val familyId: String = "",
    val userId: String = "",
    val userName: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val recurringRepository: RecurringRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var isProcessingRecurring = false
    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val actionState: StateFlow<Resource<String>> = _actionState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val currentUser = authRepository.getCurrentUser() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.getUserData(currentUser.uid)
                .onSuccess { user ->
                    if (user.familyId.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Anda belum terdaftar di keluarga"
                        )
                        return@onSuccess
                    }

                    _uiState.value = _uiState.value.copy(
                        familyId = user.familyId,
                        userId = user.userId,
                        userName = user.name
                    )

                    loadRecurrings()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    private fun loadRecurrings() {
        viewModelScope.launch {
            val familyId = _uiState.value.familyId

            recurringRepository.getRecurringTransactions(familyId)
                .onSuccess { recurrings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recurrings = recurrings.sortedBy { it.nextDueDate }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun addRecurring(
        name: String,
        type: String,
        amount: Double,
        category: String,
        description: String,
        frequency: String,
        dayOfMonth: Int,
        dayOfWeek: Int,
        startDate: Long,
        endDate: Long?
    ) {
        val state = _uiState.value

        viewModelScope.launch {
            _actionState.value = Resource.Loading

            val recurring = RecurringTransaction(
                familyId = state.familyId,
                userId = state.userId,
                userName = state.userName,
                name = name,
                type = type,
                amount = amount,
                category = category,
                description = description,
                frequency = frequency,
                dayOfMonth = dayOfMonth,
                dayOfWeek = dayOfWeek,
                startDate = startDate,
                endDate = endDate
            )

            recurringRepository.addRecurring(recurring)
                .onSuccess {
                    _actionState.value = Resource.Success("Recurring berhasil ditambahkan")
                    loadRecurrings()
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(
                        error.message ?: "Gagal menambah recurring"
                    )
                }
        }
    }

    fun deleteRecurring(recurringId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading

            recurringRepository.deleteRecurring(recurringId)
                .onSuccess {
                    _actionState.value = Resource.Success("Recurring berhasil dihapus")
                    loadRecurrings()
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(
                        error.message ?: "Gagal menghapus"
                    )
                }
        }
    }

    fun toggleActive(recurringId: String, isActive: Boolean) {
        viewModelScope.launch {
            recurringRepository.toggleActive(recurringId, isActive)
                .onSuccess {
                    loadRecurrings()
                }
        }
    }

    /**
     * Process due recurrings - dipanggil dari HomeScreen atau saat app aktif
     */
    fun processDueRecurrings(onComplete: (Int) -> Unit = {}) {
        val familyId = _uiState.value.familyId
        if (familyId.isEmpty()) {
            onComplete(0)
            return
        }

        if (isProcessingRecurring) {
            onComplete(0)
            return
        }

        viewModelScope.launch {
            isProcessingRecurring = true

            try {
                recurringRepository.processDueRecurrings(familyId)
                    .onSuccess { count ->
                        if (count > 0) {
                            loadRecurrings()
                        }
                        onComplete(count)
                    }
                    .onFailure {
                        onComplete(0)
                    }
            } finally {
                isProcessingRecurring = false
            }
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Idle
    }
}