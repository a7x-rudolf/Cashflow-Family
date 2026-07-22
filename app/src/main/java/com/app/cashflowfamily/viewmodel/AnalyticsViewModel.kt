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
import kotlin.math.abs

// Data untuk pie chart per kategori
data class CategoryData(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

// Data untuk bar chart per bulan
data class MonthlyData(
    val monthLabel: String,
    val monthTimestamp: Long,
    val income: Double,
    val expense: Double
)

// Data insight otomatis
data class Insight(
    val title: String,
    val description: String,
    val type: InsightType
)

enum class InsightType {
    POSITIVE, NEGATIVE, INFO, WARNING
}

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val selectedMonth: Long = System.currentTimeMillis(),
    val allTransactions: List<Transaction> = emptyList(),

    // Data untuk chart
    val expenseByCategory: List<CategoryData> = emptyList(),
    val incomeByCategory: List<CategoryData> = emptyList(),
    val monthlyComparison: List<MonthlyData> = emptyList(),

    // Summary bulan aktif
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val transactionCount: Int = 0,

    // Insights
    val insights: List<Insight> = emptyList(),

    val errorMessage: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

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

                    transactionRepository.getTransactions(user.familyId)
                        .onSuccess { transactions ->
                            _uiState.value = _uiState.value.copy(
                                allTransactions = transactions
                            )
                            processData()
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
        processData()
    }

    private fun processData() {
        val state = _uiState.value
        val selectedMonth = state.selectedMonth

        // Filter transaksi bulan aktif
        val monthTransactions = state.allTransactions.filter {
            DateFormatter.isSameMonth(it.date, selectedMonth)
        }

        // Hitung summary
        val income = monthTransactions.filter { it.type == "income" }.sumOf { it.amount }
        val expense = monthTransactions.filter { it.type == "expense" }.sumOf { it.amount }

        // Group by category untuk pengeluaran
        val expenseByCategory = groupByCategory(
            monthTransactions.filter { it.type == "expense" }
        )

        // Group by category untuk pemasukan
        val incomeByCategory = groupByCategory(
            monthTransactions.filter { it.type == "income" }
        )

        // Data 6 bulan terakhir untuk bar chart
        val monthlyComparison = generateMonthlyComparison(state.allTransactions, selectedMonth)

        // Generate insights
        val insights = generateInsights(
            income = income,
            expense = expense,
            balance = income - expense,
            expenseByCategory = expenseByCategory,
            monthlyComparison = monthlyComparison,
            transactionCount = monthTransactions.size
        )

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            expenseByCategory = expenseByCategory,
            incomeByCategory = incomeByCategory,
            monthlyComparison = monthlyComparison,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            transactionCount = monthTransactions.size,
            insights = insights
        )
    }

    private fun groupByCategory(transactions: List<Transaction>): List<CategoryData> {
        if (transactions.isEmpty()) return emptyList()

        val total = transactions.sumOf { it.amount }

        return transactions
            .groupBy { it.category }
            .map { (category, txList) ->
                val amount = txList.sumOf { it.amount }
                CategoryData(
                    category = category,
                    amount = amount,
                    percentage = ((amount / total) * 100).toFloat(),
                    transactionCount = txList.size
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun generateMonthlyComparison(
        allTransactions: List<Transaction>,
        currentMonth: Long
    ): List<MonthlyData> {
        val monthsData = mutableListOf<MonthlyData>()

        // 6 bulan ke belakang termasuk bulan aktif
        for (i in 5 downTo 0) {
            val monthTimestamp = DateFormatter.addMonths(currentMonth, -i)

            val monthTx = allTransactions.filter {
                DateFormatter.isSameMonth(it.date, monthTimestamp)
            }

            val monthIncome = monthTx.filter { it.type == "income" }.sumOf { it.amount }
            val monthExpense = monthTx.filter { it.type == "expense" }.sumOf { it.amount }

            monthsData.add(
                MonthlyData(
                    monthLabel = DateFormatter.formatMonthYear(monthTimestamp)
                        .split(" ").first().take(3), // "Nov"
                    monthTimestamp = monthTimestamp,
                    income = monthIncome,
                    expense = monthExpense
                )
            )
        }

        return monthsData
    }

    private fun generateInsights(
        income: Double,
        expense: Double,
        balance: Double,
        expenseByCategory: List<CategoryData>,
        monthlyComparison: List<MonthlyData>,
        transactionCount: Int
    ): List<Insight> {
        val insights = mutableListOf<Insight>()

        // Insight 1: Balance status
        when {
            transactionCount == 0 -> {
                insights.add(Insight(
                    title = "Belum Ada Data",
                    description = "Belum ada transaksi di bulan ini. Mulai catat untuk mendapatkan insight.",
                    type = InsightType.INFO
                ))
                return insights
            }

            balance > 0 -> {
                val ratio = (balance / income * 100).toInt()
                insights.add(Insight(
                    title = "Keuangan Sehat",
                    description = "Anda berhasil menyisihkan $ratio% dari pemasukan bulan ini.",
                    type = InsightType.POSITIVE
                ))
            }

            balance < 0 -> {
                insights.add(Insight(
                    title = "Pengeluaran Melebihi Pemasukan",
                    description = "Pengeluaran Anda melebihi pemasukan bulan ini. Perlu evaluasi budget.",
                    type = InsightType.WARNING
                ))
            }

            else -> {
                insights.add(Insight(
                    title = "Keseimbangan",
                    description = "Pemasukan dan pengeluaran seimbang bulan ini.",
                    type = InsightType.INFO
                ))
            }
        }

        // Insight 2: Kategori pengeluaran terbesar
        if (expenseByCategory.isNotEmpty()) {
            val topCategory = expenseByCategory.first()
            insights.add(Insight(
                title = "Pengeluaran Terbesar",
                description = "Kategori \"${topCategory.category}\" menghabiskan ${topCategory.percentage.toInt()}% dari total pengeluaran.",
                type = if (topCategory.percentage > 50) InsightType.WARNING else InsightType.INFO
            ))
        }

        // Insight 3: Perbandingan dengan bulan lalu
        if (monthlyComparison.size >= 2) {
            val current = monthlyComparison.last()
            val previous = monthlyComparison[monthlyComparison.size - 2]

            if (previous.expense > 0) {
                val expenseDiff = current.expense - previous.expense
                val percentDiff = ((expenseDiff / previous.expense) * 100).toInt()

                when {
                    percentDiff > 20 -> {
                        insights.add(Insight(
                            title = "Pengeluaran Meningkat",
                            description = "Pengeluaran naik $percentDiff% dari bulan lalu. Perhatikan budget Anda.",
                            type = InsightType.WARNING
                        ))
                    }
                    percentDiff < -20 -> {
                        insights.add(Insight(
                            title = "Hemat!",
                            description = "Pengeluaran turun ${abs(percentDiff)}% dari bulan lalu. Kerja bagus!",
                            type = InsightType.POSITIVE
                        ))
                    }
                }
            }
        }

        return insights
    }
}