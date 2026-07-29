package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Event
import com.app.cashflowfamily.data.model.EventCategory
import com.app.cashflowfamily.data.model.EventTransfer
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

data class EventBudgetUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val event: Event? = null,
    val categories: List<EventCategory> = emptyList(),
    val transfers: List<EventTransfer> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val totalAllocated: Double = 0.0,
    val unallocated: Double = 0.0
)

@HiltViewModel
class EventBudgetViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
    private val thresholdNotifier: EventThresholdNotifier  // ← BARU
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventBudgetUiState())
    val uiState: StateFlow<EventBudgetUiState> = _uiState.asStateFlow()

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

    fun loadBudget(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val eventResult = eventRepository.getEventById(eventId)
            val categoriesResult = eventRepository.getCategories(eventId)
            val transfersResult = eventRepository.getTransfers(eventId)

            val event = eventResult.getOrNull()
            val categories = categoriesResult.getOrElse { emptyList() }
            val totalAllocated = categories.sumOf { it.allocatedBudget }

            _uiState.update {
                it.copy(
                    isLoading = false, event = event, categories = categories,
                    transfers = transfersResult.getOrElse { emptyList() },
                    totalAllocated = totalAllocated,
                    unallocated = (event?.totalBudget ?: 0.0) - totalAllocated,
                    error = eventResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun addCategory(eventId: String, name: String, iconKey: String, colorHex: String, budget: Double) {
        viewModelScope.launch {
            val ready = ensureUserReady()
            if (!ready) {
                _uiState.update { it.copy(error = "Data pengguna belum siap") }
                return@launch
            }
            if (name.isBlank()) {
                _uiState.update { it.copy(error = "Nama kategori harus diisi") }
                return@launch
            }
            if (budget < 0) {
                _uiState.update { it.copy(error = "Budget tidak boleh negatif") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true) }
            val category = EventCategory(
                eventId = eventId, familyId = familyId, name = name.trim(),
                iconKey = iconKey, colorHex = colorHex, allocatedBudget = budget,
                sortOrder = _uiState.value.categories.size
            )
            eventRepository.addCategory(category)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Kategori ditambahkan") }
                    loadBudget(eventId)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun updateCategory(category: EventCategory) {
        viewModelScope.launch {
            if (category.name.isBlank()) {
                _uiState.update { it.copy(error = "Nama kategori harus diisi") }
                return@launch
            }
            if (category.allocatedBudget < 0) {
                _uiState.update { it.copy(error = "Budget tidak boleh negatif") }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true) }
            eventRepository.updateCategory(category)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Kategori diperbarui") }
                    loadBudget(category.eventId)

                    // === TRIGGER NOTIFIKASI ===
                    // Update allocatedBudget bisa mengubah tier
                    launch {
                        thresholdNotifier.checkAndNotify(
                            eventId = category.eventId,
                            categoryId = category.categoryId,
                            actorUserId = userId
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun deleteCategory(eventId: String, categoryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            eventRepository.deleteCategory(eventId, categoryId)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Kategori dihapus") }
                    loadBudget(eventId)

                    // === TRIGGER NOTIFIKASI ===
                    // Delete kategori bisa mempengaruhi event total (kurangi spent)
                    launch {
                        thresholdNotifier.checkAndNotify(
                            eventId = eventId,
                            categoryId = null,  // kategori sudah tidak ada
                            actorUserId = userId
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun transferBudget(
        eventId: String, from: EventCategory, to: EventCategory,
        amount: Double, note: String
    ) {
        viewModelScope.launch {
            val ready = ensureUserReady()
            if (!ready) {
                _uiState.update { it.copy(error = "Data pengguna belum siap") }
                return@launch
            }
            if (amount <= 0) {
                _uiState.update { it.copy(error = "Jumlah transfer harus lebih dari 0") }
                return@launch
            }
            if (from.categoryId == to.categoryId) {
                _uiState.update { it.copy(error = "Kategori asal & tujuan tidak boleh sama") }
                return@launch
            }
            if (amount > from.availableForTransfer) {
                _uiState.update {
                    it.copy(error = "Dana tidak cukup. Tersedia: ${from.availableForTransfer.toLong()}")
                }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true) }
            val transfer = EventTransfer(
                eventId = eventId, familyId = familyId, createdBy = userId,
                fromCategoryId = from.categoryId, fromCategoryName = from.name,
                toCategoryId = to.categoryId, toCategoryName = to.name,
                amount = amount, note = note.trim()
            )
            eventRepository.transferBudget(transfer)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Transfer berhasil") }
                    loadBudget(eventId)

                    // === TRIGGER NOTIFIKASI ===
                    // Transfer mengubah budget di 2 kategori sekaligus + event
                    launch {
                        thresholdNotifier.checkAllCategoriesAndEvent(
                            eventId = eventId,
                            actorUserId = userId
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun deleteTransfer(transfer: EventTransfer, eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            eventRepository.deleteTransfer(transfer)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Transfer dibatalkan") }
                    loadBudget(eventId)

                    // === TRIGGER NOTIFIKASI ===
                    launch {
                        thresholdNotifier.checkAllCategoriesAndEvent(
                            eventId = eventId,
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
}