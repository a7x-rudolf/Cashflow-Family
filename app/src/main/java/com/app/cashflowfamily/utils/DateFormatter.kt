package com.app.cashflowfamily.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val localeID = Locale("in", "ID")

    // Format: "15 November 2024"
    fun formatFullDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMMM yyyy", localeID)
        return sdf.format(Date(timestamp))
    }

    // Format: "15 Nov 2024"
    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy", localeID)
        return sdf.format(Date(timestamp))
    }

    // Format: "15 Nov 2024, 14:30"
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy, HH:mm", localeID)
        return sdf.format(Date(timestamp))
    }

    // Format: "November 2024"
    fun formatMonthYear(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", localeID)
        return sdf.format(Date(timestamp))
    }

    // Cek apakah tanggal di bulan ini
    fun isCurrentMonth(timestamp: Long): Boolean {
        val currentMonth = SimpleDateFormat("MM-yyyy", localeID).format(Date())
        val checkMonth = SimpleDateFormat("MM-yyyy", localeID).format(Date(timestamp))
        return currentMonth == checkMonth
    }

    // Format label group: "Hari Ini", "Kemarin", atau tanggal
    fun formatDateGroup(timestamp: Long): String {
        val today = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val todayDateStr = SimpleDateFormat("yyyy-MM-dd", localeID).format(Date(today))
        val yesterdayDateStr = SimpleDateFormat("yyyy-MM-dd", localeID).format(Date(today - oneDayMillis))
        val checkDateStr = SimpleDateFormat("yyyy-MM-dd", localeID).format(Date(timestamp))

        return when (checkDateStr) {
            todayDateStr -> "Hari Ini"
            yesterdayDateStr -> "Kemarin"
            else -> formatFullDate(timestamp)
        }
    }

    // Ambil awal bulan (timestamp)
    fun getStartOfMonth(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    // Ambil akhir bulan (timestamp)
    fun getEndOfMonth(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    // Tambah bulan (bisa negatif untuk mundur)
    fun addMonths(timestamp: Long, months: Int): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            add(java.util.Calendar.MONTH, months)
        }
        return calendar.timeInMillis
    }

    // Cek apakah 2 tanggal di bulan yang sama
    fun isSameMonth(timestamp1: Long, timestamp2: Long): Boolean {
        val format = SimpleDateFormat("MM-yyyy", localeID)
        return format.format(Date(timestamp1)) == format.format(Date(timestamp2))
    }

    // Ambil month (1-12) dari timestamp
    fun getMonth(timestamp: Long): Int {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        return calendar.get(java.util.Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
    }

    // Ambil year dari timestamp
    fun getYear(timestamp: Long): Int {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        return calendar.get(java.util.Calendar.YEAR)
    }
}