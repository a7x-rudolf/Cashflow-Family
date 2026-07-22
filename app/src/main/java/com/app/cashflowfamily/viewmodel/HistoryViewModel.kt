package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.TransactionRepository
import com.app.cashflowfamily.utils.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TransactionFilter {
    ALL, INCOME, EXPENSE
}

data class HistoryUiState(
    val isLoading: Boolean = false,
    val allTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val groupedTransactions: Map<String, List<Transaction>> = emptyMap(),
    val selectedMonth: Long = System.currentTimeMillis(),
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val familyName: String = "",

    // BARU: Advanced Filter
    val searchQuery: String = "",
    val advancedFilter: AdvancedFilter = AdvancedFilter(),
    val availableCategories: List<String> = emptyList(),
    val availableUsers: List<UserOption> = emptyList(),

    val errorMessage: String? = null
) {
    val hasActiveAdvancedFilter: Boolean
        get() = advancedFilter.hasAnyFilter() || searchQuery.isNotBlank()
}

data class AdvancedFilter(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val selectedCategories: Set<String> = emptySet(),
    val selectedUserIds: Set<String> = emptySet()
) {
    fun hasAnyFilter(): Boolean {
        return startDate != null || endDate != null ||
                minAmount != null || maxAmount != null ||
                selectedCategories.isNotEmpty() || selectedUserIds.isNotEmpty()
    }

    fun activeFilterCount(): Int {
        var count = 0
        if (startDate != null || endDate != null) count++
        if (minAmount != null || maxAmount != null) count++
        if (selectedCategories.isNotEmpty()) count++
        if (selectedUserIds.isNotEmpty()) count++
        return count
    }
}

data class UserOption(
    val userId: String,
    val userName: String
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository,
    private val familyRepository: com.app.cashflowfamily.data.repository.FamilyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
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

                    // Load family info
                    familyRepository.getFamilyById(user.familyId)
                        .onSuccess { family ->
                            _uiState.value = _uiState.value.copy(familyName = family.familyName)
                        }

                    // Load transactions
                    transactionRepository.getTransactions(user.familyId)
                        .onSuccess { transactions ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                allTransactions = transactions
                            )
                            extractAvailableOptions(transactions)
                            applyFiltersAndGrouping()
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

    fun changeMonth(direction: Int) {
        val newMonth = DateFormatter.addMonths(_uiState.value.selectedMonth, direction)
        _uiState.value = _uiState.value.copy(selectedMonth = newMonth)
        applyFiltersAndGrouping()
    }

    fun setFilter(filter: TransactionFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyFiltersAndGrouping()
    }

    private fun applyFiltersAndGrouping() {
        val state = _uiState.value
        val selectedMonth = state.selectedMonth
        val hasAdvancedFilter = state.advancedFilter.hasAnyFilter()

        // Start dari semua transaksi
        var filtered = state.allTransactions

        // 1. Filter berdasarkan bulan (kecuali kalau ada advanced filter dengan date range)
        if (!hasAdvancedFilter || (state.advancedFilter.startDate == null && state.advancedFilter.endDate == null)) {
            filtered = filtered.filter {
                DateFormatter.isSameMonth(it.date, selectedMonth)
            }
        }

        // 2. Filter berdasarkan jenis (All/Income/Expense)
        filtered = when (state.selectedFilter) {
            TransactionFilter.ALL -> filtered
            TransactionFilter.INCOME -> filtered.filter { it.type == "income" }
            TransactionFilter.EXPENSE -> filtered.filter { it.type == "expense" }
        }

        // 3. Advanced Filter: Date Range
        if (state.advancedFilter.startDate != null) {
            filtered = filtered.filter { it.date >= state.advancedFilter.startDate }
        }
        if (state.advancedFilter.endDate != null) {
            filtered = filtered.filter { it.date <= state.advancedFilter.endDate }
        }

        // 4. Advanced Filter: Amount Range
        if (state.advancedFilter.minAmount != null) {
            filtered = filtered.filter { it.amount >= state.advancedFilter.minAmount }
        }
        if (state.advancedFilter.maxAmount != null) {
            filtered = filtered.filter { it.amount <= state.advancedFilter.maxAmount }
        }

        // 5. Advanced Filter: Categories
        if (state.advancedFilter.selectedCategories.isNotEmpty()) {
            filtered = filtered.filter {
                it.category in state.advancedFilter.selectedCategories
            }
        }

        // 6. Advanced Filter: Users
        if (state.advancedFilter.selectedUserIds.isNotEmpty()) {
            filtered = filtered.filter {
                it.userId in state.advancedFilter.selectedUserIds
            }
        }

        // 7. Search Query
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase().trim()
            filtered = filtered.filter { transaction ->
                transaction.category.lowercase().contains(query) ||
                        transaction.description.lowercase().contains(query) ||
                        transaction.userName.lowercase().contains(query)
            }
        }

        // 8. Hitung total (dari data yang sudah difilter)
        val income = filtered.filter { it.type == "income" }.sumOf { it.amount }
        val expense = filtered.filter { it.type == "expense" }.sumOf { it.amount }

        // 9. Group berdasarkan tanggal
        val grouped = filtered
            .sortedByDescending { it.date }
            .groupBy { DateFormatter.formatDateGroup(it.date) }

        _uiState.value = _uiState.value.copy(
            filteredTransactions = filtered,
            groupedTransactions = grouped,
            totalIncome = income,
            totalExpense = expense
        )
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFiltersAndGrouping()
    }

    fun setAdvancedFilter(filter: AdvancedFilter) {
        _uiState.value = _uiState.value.copy(advancedFilter = filter)
        applyFiltersAndGrouping()
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            advancedFilter = AdvancedFilter(),
            selectedFilter = TransactionFilter.ALL
        )
        applyFiltersAndGrouping()
    }

    private fun extractAvailableOptions(transactions: List<Transaction>) {
        val categories = transactions.map { it.category }.distinct().sorted()
        val users = transactions.map { UserOption(it.userId, it.userName) }.distinct().sortedBy { it.userName }

        _uiState.value = _uiState.value.copy(
            availableCategories = categories,
            availableUsers = users
        )
    }

    fun refresh() {
        loadTransactions()
    }
}