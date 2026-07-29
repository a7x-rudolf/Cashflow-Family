package com.app.cashflowfamily.ui.event

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Warna untuk modul Event yang theme-aware.
 *
 * Fix bug 1.3: sebelumnya pakai hex literal (Color(0xFFFFEBEE) dll)
 * yang tidak menyesuaikan dark mode. Sekarang:
 * - Warna semantic (error/warning/success/info) diturunkan dari MaterialTheme.colorScheme
 * - Untuk nuansa "soft" yang beda dari default MD3, pakai pair light/dark
 *   yang dipilih via isSystemInDarkTheme()
 *
 * Prinsip: JANGAN pakai hex literal langsung di UI Event lagi. Selalu
 * lewat properti @Composable di object ini agar dark mode konsisten.
 */
object EventColors {

    // ==========================================================
    // ERROR (over budget, delete, dll)
    // ==========================================================

    val ErrorBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.errorContainer

    val ErrorText: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.error

    val OnErrorBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onErrorContainer

    // ==========================================================
    // WARNING (>80%, kelebihan alokasi, dll)
    // Amber/orange soft — MD3 tidak punya semantik warning bawaan,
    // jadi kita definisikan light/dark pair sendiri
    // ==========================================================

    val WarningBg: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF4A3A1F) else Color(0xFFFFF4E5)

    val WarningText: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFFFCC80) else Color(0xFFB25000)

    // ==========================================================
    // SUCCESS (budget aman, transfer sukses, dll)
    // MD3 juga tidak punya semantik success, pair light/dark manual
    // ==========================================================

    val SuccessBg: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF1B3D22) else Color(0xFFE8F5E9)

    val SuccessText: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)

    // ==========================================================
    // INFO (countdown, tip, planning status)
    // Pakai primaryContainer sebagai info — konsisten dengan tema app
    // ==========================================================

    val InfoBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primaryContainer

    val InfoText: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onPrimaryContainer

    // ==========================================================
    // STATUS EVENT (Planning / InProgress / Completed / Archived)
    // Warna semantic per status
    // ==========================================================

    val StatusPlanning: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary

    val StatusInProgress: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFFFB74D) else Color(0xFFF57C00)

    val StatusCompleted: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF388E3C)

    val StatusArchived: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outline
}