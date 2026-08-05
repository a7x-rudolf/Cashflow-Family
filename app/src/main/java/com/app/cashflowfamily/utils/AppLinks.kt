package com.app.cashflowfamily.utils

import com.app.cashflowfamily.BuildConfig

/**
 * Link-link yang dipakai berulang di berbagai layar (mis. pesan bagikan
 * kode undangan). Dipusatkan di sini supaya kalau URL rilis berubah,
 * cukup update 1 tempat -- bukan cari-cari di tiap file share intent.
 *
 * Nilainya beda otomatis per build flavor (lihat productFlavors di
 * app/build.gradle.kts): flavor "github" mengarah ke APK GitHub Releases,
 * flavor "playstore" mengarah ke listing Play Store.
 */
object AppLinks {
    val INVITE_APK_DOWNLOAD_URL: String = BuildConfig.INVITE_APK_DOWNLOAD_URL
}
