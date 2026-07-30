package com.app.cashflowfamily.utils

import com.app.cashflowfamily.data.model.RecurringFrequency
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat

object RecurringCalculator {

    /**
     * Hitung tanggal jatuh tempo berikutnya berdasarkan frekuensi.
     * Menggunakan UTC untuk memastikan konsistensi ID di seluruh device keluarga.
     */
    fun calculateNextDueDate(
        frequency: String,
        currentDate: Long,
        dayOfMonth: Int = 1,
        dayOfWeek: Int = 1
    ): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = currentDate
            set(Calendar.HOUR_OF_DAY, 0) // Gunakan 00:00 UTC sebagai standar
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (RecurringFrequency.fromValue(frequency)) {
            RecurringFrequency.DAILY -> {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            RecurringFrequency.WEEKLY -> {
                calendar.add(Calendar.DAY_OF_MONTH, 7)
            }

            RecurringFrequency.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val safeDayOfMonth = minOf(dayOfMonth, maxDay)
                calendar.set(Calendar.DAY_OF_MONTH, safeDayOfMonth)
            }
        }
        return calendar.timeInMillis
    }

    /**
     * Hitung tanggal jatuh tempo pertama dari startDate.
     * Menggunakan UTC.
     */
    fun calculateFirstDueDate(
        frequency: String,
        startDate: Long,
        dayOfMonth: Int = 1,
        dayOfWeek: Int = 1
    ): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (RecurringFrequency.fromValue(frequency) == RecurringFrequency.MONTHLY) {
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val safeDayOfMonth = minOf(dayOfMonth, maxDay)
            calendar.set(Calendar.DAY_OF_MONTH, safeDayOfMonth)
        }
        
        return calendar.timeInMillis
    }

    /**
     * Cek apakah recurring sudah harus di-generate (due date <= sekarang)
     */
    fun isDue(nextDueDate: Long): Boolean {
        // Bandingkan dengan sekarang (juga dinormalisasi ke UTC 00:00 jika ingin sangat ketat,
        // tapi isDue biasanya hanya untuk memicu proses).
        return nextDueDate <= System.currentTimeMillis()
    }

    /**
     * Buat suffix ID unik berdasarkan tanggal (UTC).
     * Contoh: "2026-07-31"
     */
    fun formatDateId(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(timestamp))
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