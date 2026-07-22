package com.app.cashflowfamily.utils.feedback

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Meneruskan isi feedback dari user ke developer (Rudolf) via Email atau WhatsApp,
 * menggunakan Intent bawaan Android (bukan pengiriman otomatis lewat server).
 *
 * User tetap harus menekan tombol "Kirim" di aplikasi Email/WhatsApp yang terbuka,
 * karena Android tidak mengizinkan aplikasi lain mengirim email/pesan WA secara diam-diam.
 */
object FeedbackForwardHelper {

    // Kontak developer
    private const val DEV_EMAIL = "Rudolflumba52@gmail.com"
    private const val DEV_WHATSAPP_NUMBER = "6285714745110" // format internasional, tanpa '+' atau '0' di depan

    private fun buildMessage(
        typeLabel: String,
        title: String,
        message: String,
        userName: String,
        userEmail: String
    ): String {
        val senderInfo = if (userName.isNotBlank() || userEmail.isNotBlank()) {
            "\n\nDikirim oleh: ${userName.ifBlank { "-" }} (${userEmail.ifBlank { "-" }})"
        } else {
            ""
        }
        return "Jenis: $typeLabel\nJudul: $title\n\nPesan:\n$message$senderInfo"
    }

    /**
     * Membuka aplikasi email dengan alamat, subjek, dan isi pesan yang sudah terisi.
     */
    fun openEmail(
        context: Context,
        typeLabel: String,
        title: String,
        message: String,
        userName: String = "",
        userEmail: String = ""
    ) {
        val body = buildMessage(typeLabel, title, message, userName, userEmail)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(DEV_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "[CashFlow Family] $title")
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Kirim via Email"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Tidak ada aplikasi email yang terpasang", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Membuka WhatsApp (atau browser bila WhatsApp tidak terpasang) dengan pesan
     * yang sudah terisi, mengarah ke nomor developer.
     */
    fun openWhatsApp(
        context: Context,
        typeLabel: String,
        title: String,
        message: String,
        userName: String = "",
        userEmail: String = ""
    ) {
        val body = buildMessage(typeLabel, title, message, userName, userEmail)
        val url = "https://wa.me/$DEV_WHATSAPP_NUMBER?text=${Uri.encode(body)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }
}
