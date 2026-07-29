package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Event
import com.app.cashflowfamily.data.model.EventCategory
import com.app.cashflowfamily.data.model.EventTransaction
import com.app.cashflowfamily.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventDashboardUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val categories: List<EventCategory> = emptyList(),
    val recentTransactions: List<EventTransaction> = emptyList(),
    val error: String? = null,
    val overBudgetCategories: List<EventCategory> = emptyList(),
    val warningCategories: List<EventCategory> = emptyList()
)

@HiltViewModel
class EventDashboardViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDashboardUiState())
    val uiState: StateFlow<EventDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val eventResult = eventRepository.getEventById(eventId)
            val categoriesResult = eventRepository.getCategories(eventId)
            // Fix #10: hanya fetch 5 transaksi terbaru, bukan semua
            val transactionsResult = eventRepository.getTransactions(eventId, limit = 5L)

            val event = eventResult.getOrNull()
            val categories = categoriesResult.getOrElse { emptyList() }
            val transactions = transactionsResult.getOrElse { emptyList() }

            _uiState.update {
                it.copy(
                    isLoading = false, event = event, categories = categories,
                    recentTransactions = transactions,
                    overBudgetCategories = categories.filter { c -> c.isOverBudget },
                    warningCategories = categories.filter { c -> c.isWarningBudget },
                    error = eventResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}