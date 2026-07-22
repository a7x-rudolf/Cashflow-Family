package com.app.cashflowfamily.data.model

data class Transaction(
    val transactionId: String = "",
    val familyId: String = "",
    val userId: String = "",
    val userName: String = "",
    val type: String = "expense", // "income" atau "expense"
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),

    // Untuk transaksi hasil recurring
    val recurringId: String = "",
    val recurringGeneratedFor: Long = 0L,
    val recurringGenerated: Boolean = false


)