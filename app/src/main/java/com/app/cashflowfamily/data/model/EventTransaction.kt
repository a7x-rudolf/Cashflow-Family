package com.app.cashflowfamily.data.model

data class EventTransaction(
    val transactionId: String = "",
    val eventId: String = "",
    val categoryId: String = "",
    val familyId: String = "",
    val createdBy: String = "",         // userId

    val name: String = "",              // deskripsi pengeluaran
    val amount: Double = 0.0,

    val transactionDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)