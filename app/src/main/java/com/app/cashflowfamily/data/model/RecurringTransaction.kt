package com.app.cashflowfamily.data.model

data class RecurringTransaction(
    val recurringId: String = "",
    val familyId: String = "",
    val userId: String = "",
    val userName: String = "",
    val name: String = "",              // "Gaji Bulanan", "Listrik", dll
    val type: String = "expense",       // income/expense
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val frequency: String = "MONTHLY",  // DAILY, WEEKLY, MONTHLY
    val dayOfMonth: Int = 1,           // Untuk MONTHLY: 1-31
    val dayOfWeek: Int = 1,            // Untuk WEEKLY: 1=Senin, 7=Minggu
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,          // null = tanpa batas
    val lastGeneratedDate: Long = 0L,   // Tanggal terakhir generate
    val nextDueDate: Long = 0L,         // Tanggal berikutnya jatuh tempo
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RecurringFrequency(val value: String, val label: String) {
    DAILY("DAILY", "Harian"),
    WEEKLY("WEEKLY", "Mingguan"),
    MONTHLY("MONTHLY", "Bulanan");

    companion object {
        fun fromValue(value: String): RecurringFrequency {
            return entries.find { it.value == value } ?: MONTHLY
        }
    }
}