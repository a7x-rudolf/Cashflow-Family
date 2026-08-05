package com.app.cashflowfamily.utils

import android.content.Context

/**
 * POLICY FIX (v1.1.1): versi flavor "playstore".
 *
 * Google Play melarang app yang didistribusikan lewat Play Store untuk
 * mengecek/mengunduh/menginstall update dari sumber selain Play Store
 * sendiri. Jadi untuk build ini, checkForUpdate() sengaja SELALU
 * mengembalikan null tanpa melakukan panggilan jaringan apapun ke GitHub.
 *
 * Update untuk pengguna Play Store sepenuhnya mengandalkan mekanisme
 * update Play Store bawaan.
 */
class UpdateChecker(@Suppress("UNUSED_PARAMETER") context: Context) {
    suspend fun checkForUpdate(): UpdateInfo? = null
}
