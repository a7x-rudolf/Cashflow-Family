package com.app.cashflowfamily.utils

import com.app.cashflowfamily.data.model.RecurringFrequency
import java.util.Calendar

object RecurringCalculator {

    /**
     * Hitung tanggal jatuh tempo berikutnya berdasarkan frekuensi
     */
    fun calculateNextDueDate(
        frequency: String,
        currentDate: Long,
        dayOfMonth: Int = 1,
        dayOfWeek: Int = 1
    ): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentDate
            set(Calendar.HOUR_OF_DAY, 8)  // Default jam 8 pagi
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when (RecurringFrequency.fromValue(frequency)) {
            RecurringFrequency.DAILY -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }

            RecurringFrequency.WEEKLY -> {
                calendar.add(Calendar.DAY_OF_MONTH, 7)
                calendar.timeInMillis
            }

            RecurringFrequency.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)

                // Handle case: dayOfMonth > jumlah hari di bulan target
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val safeDayOfMonth = minOf(dayOfMonth, maxDay)
                calendar.set(Calendar.DAY_OF_MONTH, safeDayOfMonth)

                calendar.timeInMillis
            }
        }
    }

    /**
     * Hitung tanggal jatuh tempo pertama dari startDate
     */
    fun calculateFirstDueDate(
        frequency: String,
        startDate: Long,
        dayOfMonth: Int = 1,
        dayOfWeek: Int = 1
    ): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when (RecurringFrequency.fromValue(frequency)) {
            RecurringFrequency.DAILY -> calendar.timeInMillis

            RecurringFrequency.WEEKLY -> calendar.timeInMillis

            RecurringFrequency.MONTHLY -> {
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val safeDayOfMonth = minOf(dayOfMonth, maxDay)
                calendar.set(Calendar.DAY_OF_MONTH, safeDayOfMonth)
                calendar.timeInMillis
            }
        }
    }

    /**
     * Cek apakah recurring sudah harus di-generate (due date <= sekarang)
     */
    fun isDue(nextDueDate: Long): Boolean {
        return nextDueDate <= System.currentTimeMillis()
    }

    /**
     * Cek apakah recurring sudah expired (past end date)
     */
    fun isExpired(endDate: Long?): Boolean {
        if (endDate == null) return false
        return System.currentTimeMillis() > endDate
    }

    /**
     * Format frekuensi untuk display
     */
    fun formatFrequencyDisplay(recurring: com.app.cashflowfamily.data.model.RecurringTransaction): String {
        return when (RecurringFrequency.fromValue(recurring.frequency)) {
            RecurringFrequency.DAILY -> "Setiap hari"

            RecurringFrequency.WEEKLY -> {
                val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
                val dayIndex = (recurring.dayOfWeek - 1).coerceIn(0, 6)
                "Setiap ${dayNames[dayIndex]}"
            }

            RecurringFrequency.MONTHLY -> "Tanggal ${recurring.dayOfMonth} setiap bulan"
        }
    }
}