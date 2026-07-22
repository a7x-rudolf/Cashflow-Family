package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.Family
import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.data.model.User
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.data.repository.FamilyRepository
import com.app.cashflowfamily.data.repository.TransactionRepository
import com.app.cashflowfamily.utils.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class untuk data per bulan (summary + transaksinya)
data class MonthData(
    val monthTimestamp: Long,
    val income: Double,
    val expense: Double,
    val balance: Double,
    val transactions: List<Transaction>  // Transaksi di bulan ini
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val family: Family? = null,
    val monthDataList: List<MonthData> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val familyRepository: FamilyRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val currentUser = authRepository.getCurrentUser() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            authRepository.getUserData(currentUser.uid)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(user = user)

                    if (user.familyId.isNotEmpty()) {
                        familyRepository.getFamilyById(user.familyId)
                            .onSuccess { family ->
                                _uiState.value = _uiState.value.copy(family = family)
                            }

                        loadTransactions(user.familyId)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
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

    private fun loadTransactions(familyId: String) {
        viewModelScope.launch {
            transactionRepository.getTransactions(familyId)
                .onSuccess { transactions ->
                    val monthDataList = generateMonthDataList(transactions)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        monthDataList = monthDataList
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

    private fun generateMonthDataList(allTransactions: List<Transaction>): List<MonthData> {
        val currentTime = System.currentTimeMillis()
        val dataList = mutableListOf<MonthData>()

        // Generate 12 bulan ke belakang (11 sampai 0)
        // Index 0 = 11 bulan lalu, Index 11 = bulan ini
        for (i in 11 downTo 0) {
            val monthTimestamp = DateFormatter.addMonths(currentTime, -i)

            // Filter transaksi bulan ini
            val monthTransactions = allTransactions
                .filter { DateFormatter.isSameMonth(it.date, monthTimestamp) }
                .sortedByDescending { it.date }

            val income = monthTransactions
                .filter { it.type == "income" }
                .sumOf { it.amount }

            val expense = monthTransactions
                .filter { it.type == "expense" }
                .sumOf { it.amount }

            dataList.add(
                MonthData(
                    monthTimestamp = monthTimestamp,
                    income = income,
                    expense = expense,
                    balance = income - expense,
                    transactions = monthTransactions
                )
            )
        }

        return dataList
    }

    fun refresh() {
        loadDashboardData()
    }
}