package com.app.cashflowfamily.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val localeID = Locale("id", "ID")

    // Alias supaya kompatibel dengan pemanggilan CurrencyFormatter.format(...)
    fun format(amount: Double): String = formatRupiah(amount)

    // Format: 1000000 -> "Rp 1.000.000"
    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(localeID)
        // Kita ijinkan decimal jika memang ada (misal hasil pembagian budget)
        format.maximumFractionDigits = 2
        return format.format(amount)
            .replace("Rp", "Rp ")
    }

    // Format: 1000000 -> "1.000.000" (tanpa Rp)
    fun formatNumber(amount: Double): String {
        val format = NumberFormat.getNumberInstance(localeID)
        format.maximumFractionDigits = 2
        return format.format(amount)
    }

    // Parse: "1.000.000,50" -> 1000000.5
    fun parseRupiah(text: String): Double {
        if (text.isBlank()) return 0.0
        
        // Bersihkan simbol mata uang dan spasi
        var cleaned = text.replace("Rp", "").replace(" ", "")
        
        // Logika parsing manual yang lebih aman untuk Locale Indonesia:
        // Titik (.) biasanya pemisah ribuan, Koma (,) biasanya pemisah desimal.
        // Tapi kita buat supaya robust: hapus semua titik ribuan, ganti koma desimal jadi titik.
        cleaned = cleaned.replace(".", "")
        cleaned = cleaned.replace(",", ".")
        
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    fun formatInput(value: Long): String {
        return java.text.DecimalFormat("#,###").format(value).replace(",", ".")
    }
}