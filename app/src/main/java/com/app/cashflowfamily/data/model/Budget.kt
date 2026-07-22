package com.app.cashflowfamily.data.model

data class Budget(
    val budgetId: String = "",
    val familyId: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val month: Int = 0,        // 1-12
    val year: Int = 0,         // 2024
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)