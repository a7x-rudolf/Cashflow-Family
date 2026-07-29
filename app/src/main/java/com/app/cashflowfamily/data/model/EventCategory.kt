package com.app.cashflowfamily.data.model

data class EventCategory(
    val categoryId: String = "",
    val eventId: String = "",
    val familyId: String = "",

    val name: String = "",
    val iconKey: String = EventCategoryIcon.OTHER.name,
    val colorHex: String = "#2196F3",

    val allocatedBudget: Double = 0.0,
    val spentAmount: Double = 0.0,
    val transferredIn: Double = 0.0,
    val transferredOut: Double = 0.0,

    val sortOrder: Int = 0,

    // Tier notifikasi ambang batas terakhir yang sudah dikirim untuk kategori
    // ini (0 = belum ada, 80 = sudah kirim warning, 100 = sudah kirim over).
    // Reset otomatis ke 0 kalau spentAmount turun di bawah threshold-nya.
    val lastNotifiedPercentage: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val effectiveBudget: Double
        get() = allocatedBudget + transferredIn - transferredOut

    val remainingBudget: Double
        get() = effectiveBudget - spentAmount

    val budgetPercentage: Double
        get() = if (effectiveBudget > 0) (spentAmount / effectiveBudget * 100) else 0.0

    val isOverBudget: Boolean get() = spentAmount > effectiveBudget

    val isWarningBudget: Boolean
        get() = budgetPercentage >= 80.0 && !isOverBudget

    val availableForTransfer: Double
        get() = if (remainingBudget > 0) remainingBudget else 0.0
}

enum class EventCategoryIcon(val iconKey: String) {
    VENUE("VENUE"),
    CATERING("CATERING"),
    DECORATION("DECORATION"),
    PHOTO_VIDEO("PHOTO_VIDEO"),
    INVITATION("INVITATION"),
    ATTIRE("ATTIRE"),
    ENTERTAINMENT("ENTERTAINMENT"),
    SOUVENIR("SOUVENIR"),
    TRANSPORT("TRANSPORT"),
    RING("RING"),
    HONEYMOON("HONEYMOON"),
    CAKE("CAKE"),
    ACCOMMODATION("ACCOMMODATION"),
    TICKET("TICKET"),
    SHOPPING("SHOPPING"),
    BUILDING("BUILDING"),
    WORKER("WORKER"),
    FURNITURE("FURNITURE"),
    OTHER("OTHER")
}