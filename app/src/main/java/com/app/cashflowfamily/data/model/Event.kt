package com.app.cashflowfamily.data.model

data class Event(
    val eventId: String = "",
    val familyId: String = "",
    val createdBy: String = "",

    val name: String = "",
    val type: String = EventType.WEDDING.name,
    val description: String = "",

    val totalBudget: Double = 0.0,
    val spentAmount: Double = 0.0,

    val eventDate: Long = 0L,
    val endDate: Long = 0L,

    val status: String = EventStatus.PLANNING.name,

    // Tier notifikasi ambang batas terakhir yang sudah dikirim untuk event ini
    // (0 = belum ada, 80 = sudah kirim warning, 100 = sudah kirim over).
    // Reset otomatis ke 0 kalau spentAmount turun di bawah threshold-nya
    // (mis. user hapus transaksi) supaya bisa dinotifikasi lagi kalau naik lagi.
    val lastNotifiedPercentage: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val remainingBudget: Double get() = totalBudget - spentAmount

    val budgetPercentage: Double
        get() = if (totalBudget > 0) (spentAmount / totalBudget * 100) else 0.0

    val isOverBudget: Boolean get() = spentAmount > totalBudget

    val isWarningBudget: Boolean
        get() = budgetPercentage >= 80.0 && !isOverBudget

    val daysUntilEvent: Int
        get() {
            if (eventDate == 0L) return -1
            val diff = eventDate - System.currentTimeMillis()
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }

    val isEventPassed: Boolean
        get() = eventDate > 0L && eventDate < System.currentTimeMillis()
}

enum class EventType(val label: String, val iconKey: String) {
    WEDDING("Pernikahan", "WEDDING"),
    BIRTHDAY("Ulang Tahun", "BIRTHDAY"),
    CORPORATE("Acara Kantor", "CORPORATE"),
    RENOVATION("Renovasi", "RENOVATION"),
    TRAVEL("Perjalanan", "TRAVEL"),
    CUSTOM("Lainnya", "CUSTOM");

    companion object {
        /**
         * Safe parse — fallback ke CUSTOM kalau string tidak valid.
         * Dipakai untuk data dari Firestore yang mungkin ter-corrupt / typo manual.
         */
        fun safeValueOf(value: String?): EventType {
            if (value.isNullOrBlank()) return CUSTOM
            return runCatching { valueOf(value) }.getOrDefault(CUSTOM)
        }
    }
}

enum class EventStatus(val label: String) {
    PLANNING("Perencanaan"),
    IN_PROGRESS("Berlangsung"),
    COMPLETED("Selesai"),
    ARCHIVED("Diarsipkan");

    companion object {
        /**
         * Safe parse — fallback ke PLANNING kalau string tidak valid.
         */
        fun safeValueOf(value: String?): EventStatus {
            if (value.isNullOrBlank()) return PLANNING
            return runCatching { valueOf(value) }.getOrDefault(PLANNING)
        }
    }
}