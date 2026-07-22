package com.app.cashflowfamily.viewmodel

import com.app.cashflowfamily.data.model.Budget
import com.app.cashflowfamily.data.model.RecurringTransaction
import com.app.cashflowfamily.data.model.Transaction

data class BackupData(
    val version: String = "1.0",
    val exportDate: Long = System.currentTimeMillis(),
    val familyName: String = "",
    val familyCode: String = "",
    val transactionsCount: Int = 0,
    val budgetsCount: Int = 0,
    val recurringsCount: Int = 0,
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val recurringTransactions: List<RecurringTransaction> = emptyList()
)

data class RestoreResult(
    val transactionsImported: Int = 0,
    val transactionsSkipped: Int = 0,
    val budgetsImported: Int = 0,
    val budgetsSkipped: Int = 0,
    val recurringsImported: Int = 0,
    val recurringsSkipped: Int = 0
) {
    val totalImported: Int
        get() = transactionsImported + budgetsImported + recurringsImported

    val totalSkipped: Int
        get() = transactionsSkipped + budgetsSkipped + recurringsSkipped
}