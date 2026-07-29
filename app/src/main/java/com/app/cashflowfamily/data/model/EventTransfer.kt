package com.app.cashflowfamily.data.model

data class EventTransfer(
    val transferId: String = "",
    val eventId: String = "",
    val familyId: String = "",
    val createdBy: String = "",         // userId

    val fromCategoryId: String = "",
    val fromCategoryName: String = "",  // snapshot nama saat transfer
    val toCategoryId: String = "",
    val toCategoryName: String = "",    // snapshot nama saat transfer

    val amount: Double = 0.0,
    val note: String = "",

    val createdAt: Long = System.currentTimeMillis()
)