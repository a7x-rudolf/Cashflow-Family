package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Budget
import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.BudgetRepository
import com.app.cashflowfamily.data.repository.TransactionRepository
import com.app.cashflowfamily.utils.DateFormatter
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class untuk kombinasi budget + actual usage
data class BudgetProgress(
    val budget: Budget,
    val spent: Double,          // Berapa yang sudah dipakai
    val remaining: Double,      // Sisa budget
    val percentage: Float,      // 0-100+
    val status: BudgetStatus,
    val transactionCount: Int   // Jumlah transaksi di kategori ini
)

enum class BudgetStatus {
    SAFE,       // < 60%
    WARNING,    // 60-89%
    DANGER,     // 90-99%
    OVER        // >= 100%
}

data class BudgetUiState(
    val isLoading: Boolean = false,
    val selectedMonth: Long = System.currentTimeMillis(),
    val budgets: List<Budget> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budgetProgress: List<BudgetProgress> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val familyId: String = "",
    val userId: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

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
                        userId = user.userId
                    )

                    loadBudgetsAndTransactions()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    private fun loadBudgetsAndTransactions() {
        viewModelScope.launch {
            val state = _uiState.value
            val month = DateFormatter.getMonth(state.selectedMonth)
            val year = DateFormatter.getYear(state.selectedMonth)

            // Load budgets
            budgetRepository.getBudgets(state.familyId, month, year)
                .onSuccess { budgets ->
                    _uiState.value = _uiState.value.copy(budgets = budgets)

                    // Load transactions
                    transactionRepository.getTransactions(state.familyId)
                        .onSuccess { transactions ->
                            _uiState.value = _uiState.value.copy(transactions = transactions)
                            calculateProgress()
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

    private fun calculateProgress() {
        val state = _uiState.value
        val selectedMonth = state.selectedMonth

        // Filter transaksi bulan aktif & type expense saja
        val monthExpenses = state.transactions.filter {
            it.type == "expense" && DateFormatter.isSameMonth(it.date, selectedMonth)
        }

        // Hitung progress per budget
        val progressList = state.budgets.map { budget ->
            val categoryTransactions = monthExpenses.filter { it.category == budget.category }
            val spent = categoryTransactions.sumOf { it.amount }
            val remaining = budget.amount - spent
            val percentage = if (budget.amount > 0) {
                ((spent / budget.amount) * 100).toFloat()
            } else 0f

            val status = when {
                percentage >= 100 -> BudgetStatus.OVER
                percentage >= 90 -> BudgetStatus.DANGER
                percentage >= 60 -> BudgetStatus.WARNING
                else -> BudgetStatus.SAFE
            }

            BudgetProgress(
                budget = budget,
                spent = spent,
                remaining = remaining,
                percentage = percentage,
                status = status,
                transactionCount = categoryTransactions.size
            )
        }.sortedByDescending { it.percentage }

        val totalBudget = state.budgets.sumOf { it.amount }
        val totalSpent = progressList.sumOf { it.spent }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            budgetProgress = progressList,
            totalBudget = totalBudget,
            totalSpent = totalSpent
        )
    }

    fun changeMonth(direction: Int) {
        val newMonth = DateFormatter.addMonths(_uiState.value.selectedMonth, direction)
        _uiState.value = _uiState.value.copy(selectedMonth = newMonth)
        loadBudgetsAndTransactions()
    }

    fun addBudget(category: String, amount: Double) {
        val state = _uiState.value
        val month = DateFormatter.getMonth(state.selectedMonth)
        val year = DateFormatter.getYear(state.selectedMonth)

        viewModelScope.launch {
            _actionState.value = Resource.Loading

            val budget = Budget(
                familyId = state.familyId,
                category = category,
                amount = amount,
                month = month,
                year = year,
                createdBy = state.userId
            )

            budgetRepository.addBudget(budget)
                .onSuccess {
                    _actionState.value = Resource.Success("Budget berhasil ditambahkan")
                    loadBudgetsAndTransactions()
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(
                        error.message ?: "Gagal menambah budget"
                    )
                }
        }
    }

    fun updateBudget(budget: Budget, newAmount: Double) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading

            val updated = budget.copy(amount = newAmount)

            budgetRepository.updateBudget(updated)
                .onSuccess {
                    _actionState.value = Resource.Success("Budget berhasil diperbarui")
                    loadBudgetsAndTransactions()
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(
                        error.message ?: "Gagal memperbarui budget"
                    )
                }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading

            budgetRepository.deleteBudget(budgetId)
                .onSuccess {
                    _actionState.value = Resource.Success("Budget berhasil dihapus")
                    loadBudgetsAndTransactions()
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(
                        error.message ?: "Gagal menghapus budget"
                    )
                }
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Idle
    }
}