package com.app.cashflowfamily.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val localeID = Locale("in", "ID")

    // Format: 1000000 -> "Rp 1.000.000"
    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0
        return format.format(amount)
            .replace("Rp", "Rp ")
            .replace(",00", "")
    }

    // Format: 1000000 -> "1.000.000" (tanpa Rp)
    fun formatNumber(amount: Double): String {
        val format = NumberFormat.getNumberInstance(localeID)
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    // Parse: "1.000.000" -> 1000000.0
    fun parseRupiah(text: String): Double {
        val cleaned = text.replace(".", "").replace("Rp", "").replace(" ", "").trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }
}