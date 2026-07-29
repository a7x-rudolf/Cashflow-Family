package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Event
import com.app.cashflowfamily.data.model.EventCategory
import com.app.cashflowfamily.data.model.EventStatus
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.EventRepository
import com.app.cashflowfamily.ui.event.CategoryTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventListUiState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    // Cached user info dengan Deferred pattern untuk fix race condition
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
                _uiState.update { it.copy(error = "Anda belum login") }
                return@launch
            }

            authRepository.getUserData(firebaseUser.uid)
                .onSuccess { user ->
                    familyId = user.familyId
                    userId = user.userId
                    userInfoReady.complete(familyId.isNotBlank())
                    if (familyId.isNotBlank()) loadEvents()
                }
                .onFailure { e ->
                    userInfoReady.complete(false)
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    /**
     * Tunggu sampai user info ready (fix race condition).
     * Return true jika familyId valid, false kalau tidak.
     */
    private suspend fun ensureUserReady(): Boolean {
        return userInfoReady.await()
    }

    fun loadEvents() {
        viewModelScope.launch {
            val ready = ensureUserReady()
            if (!ready) {
                _uiState.update { it.copy(error = "Bergabung ke keluarga terlebih dahulu") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            eventRepository.getEvents(familyId)
                .onSuccess { events ->
                    _uiState.update { it.copy(isLoading = false, events = events) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun createEvent(
        name: String, type: String, description: String,
        totalBudget: Double, eventDate: Long, endDate: Long,
        selectedTemplates: List<CategoryTemplate>,
        customBudgets: Map<Int, Double>,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val ready = ensureUserReady()
            if (!ready) {
                _uiState.update { it.copy(error = "Bergabung ke keluarga terlebih dahulu") }
                return@launch
            }

            // Validasi input
            if (name.isBlank()) {
                _uiState.update { it.copy(error = "Nama event harus diisi") }
                return@launch
            }
            if (totalBudget <= 0) {
                _uiState.update { it.copy(error = "Total budget harus lebih dari 0") }
                return@launch
            }
            if (eventDate > 0 && endDate > 0 && endDate < eventDate) {
                _uiState.update { it.copy(error = "Tanggal selesai tidak boleh sebelum tanggal event") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            val event = Event(
                familyId = familyId, createdBy = userId, name = name.trim(),
                type = type, description = description.trim(), totalBudget = totalBudget,
                eventDate = eventDate, endDate = endDate, status = EventStatus.PLANNING.name
            )

            eventRepository.createEvent(event)
                .onSuccess { createdEvent ->
                    val categories = selectedTemplates.mapIndexed { index, template ->
                        EventCategory(
                            eventId = createdEvent.eventId, familyId = familyId,
                            name = template.name, iconKey = template.iconKey,
                            colorHex = template.colorHex,
                            allocatedBudget = customBudgets[index] ?: 0.0,
                            sortOrder = index
                        )
                    }
                    if (categories.isNotEmpty()) {
                        eventRepository.addCategoriesBatch(createdEvent.eventId, categories)
                            .onSuccess {
                                _uiState.update {
                                    it.copy(isLoading = false, successMessage = "Event berhasil dibuat")
                                }
                                onSuccess(createdEvent.eventId)
                            }
                            .onFailure { e ->
                                _uiState.update { it.copy(isLoading = false, error = e.message) }
                            }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, successMessage = "Event berhasil dibuat")
                        }
                        onSuccess(createdEvent.eventId)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun updateEvent(event: Event, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Validasi input
            if (event.name.isBlank()) {
                _uiState.update { it.copy(error = "Nama event harus diisi") }
                return@launch
            }
            if (event.totalBudget <= 0) {
                _uiState.update { it.copy(error = "Total budget harus lebih dari 0") }
                return@launch
            }
            if (event.eventDate > 0 && event.endDate > 0 && event.endDate < event.eventDate) {
                _uiState.update { it.copy(error = "Tanggal selesai tidak boleh sebelum tanggal event") }
                return@launch
            }

            // Cek: totalBudget tidak boleh kurang dari total allocated existing
            val categoriesResult = eventRepository.getCategories(event.eventId)
            val totalAllocated = categoriesResult.getOrElse { emptyList() }
                .sumOf { it.allocatedBudget }

            if (event.totalBudget < totalAllocated) {
                _uiState.update {
                    it.copy(error = "Budget tidak boleh kurang dari total alokasi (${totalAllocated.toLong()})")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            eventRepository.updateEvent(event)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Event diperbarui")
                    }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun updateEventStatus(eventId: String, status: EventStatus) {
        viewModelScope.launch {
            eventRepository.updateEventStatus(eventId, status.name)
                .onSuccess { loadEvents() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteEvent(eventId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            eventRepository.deleteEvent(eventId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Event dihapus")
                    }
                    onSuccess()
                    loadEvents()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }
}