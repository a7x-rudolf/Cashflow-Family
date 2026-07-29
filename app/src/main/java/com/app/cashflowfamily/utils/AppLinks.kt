package com.app.cashflowfamily.utils

/**
 * Link-link yang dipakai berulang di berbagai layar (mis. pesan bagikan
 * kode undangan). Dipusatkan di sini supaya kalau URL rilis berubah,
 * cukup update 1 tempat -- bukan cari-cari di tiap file share intent.
 */
object AppLinks {
    // Link APK release GitHub. App sudah support in-app update check,
    // jadi link versi tetap ini aman dipakai meski rilis baru datang.
    const val INVITE_APK_DOWNLOAD_URL =
        "https://github.com/a7x-rudolf/Cashflow-Family/releases/download/v1.0.0/Cashflow.Family.v1.0.0.apk"
}
