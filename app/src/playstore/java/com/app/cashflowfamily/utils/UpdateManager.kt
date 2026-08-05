package com.app.cashflowfamily.utils

import android.content.Context

/**
 * POLICY FIX (v1.1.1): versi flavor "playstore".
 *
 * Tidak melakukan download/instalasi APK apapun -- flavor ini tidak
 * membutuhkan permission REQUEST_INSTALL_PACKAGES sama sekali (lihat
 * app/src/playstore/AndroidManifest.xml, yang sengaja TIDAK
 * mendeklarasikan permission itu).
 */
class UpdateManager(@Suppress("UNUSED_PARAMETER") context: Context) {

    fun downloadAndInstall(
        downloadUrl: String,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        onError("Update otomatis tidak tersedia di versi Play Store. Silakan update lewat Play Store.")
    }
}
