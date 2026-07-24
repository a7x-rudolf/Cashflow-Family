package com.app.cashflowfamily.data.model

data class Budget(
    val budgetId: String = "",
    val familyId: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val month: Int = 0,        // 1-12
    val year: Int = 0,         // 2024
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),

    // Tier notifikasi ambang batas terakhir yang sudah dikirim untuk budget
    // ini (0 = belum ada, 80 = sudah kirim warning, 100 = sudah kirim over).
    // Otomatis reset tiap bulan karena Budget dibuat sebagai dokumen baru
    // per month/year.
    val lastNotifiedPercentage: Int = 0
)