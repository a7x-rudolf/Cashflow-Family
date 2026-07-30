package com.app.cashflowfamily.ui.theme

import androidx.compose.ui.graphics.Color

// ===== PRIMARY - Ice Blue / Sky Blue =====
val GreenPrimary = Color(0xFF4A90D9)          // Ganti dari hijau ke ice blue
val GreenPrimaryDark = Color(0xFF2C5F8A)      // Ganti ke dark ice blue
val GreenPrimaryLight = Color(0xFF7BB3F0)     // Ganti ke light ice blue
val GreenOnPrimary = Color(0xFFFFFFFF)        // Tetap

// ===== SECONDARY - Soft Blue =====
val BlueSecondary = Color(0xFF5BB8D9)         // Ganti ke soft blue
val BlueSecondaryDark = Color(0xFF3A8CA8)     // Ganti ke dark soft blue
val BlueSecondaryLight = Color(0xFF8ED0EA)    // Ganti ke light soft blue

// ===== INCOME & EXPENSE =====
val IncomeGreen = Color(0xFF2ECC71)           // Ganti ke green lebih modern
val ExpenseRed = Color(0xFFE53935)            // Tetap

// ===== BALANCE CARD - Premium Blue Sky Gradient =====
// Dipakai khusus untuk background card saldo bulan ini di Beranda.
// Turunan dari palet ice-blue yang sudah ada (bukan warna baru di luar brand).
val BalanceCardGradientStart = Color(0xFF1E4E8C)  // Deep sky blue (anchor gelap)
val BalanceCardGradientMid = Color(0xFF4A90D9)    // = GreenPrimary
val BalanceCardGradientEnd = Color(0xFF8ED0EA)    // = BlueSecondaryLight (terang, kesan langit)

// ===== NEUTRAL =====
val BackgroundLight = Color(0xFFF8FAFC)       // Cleaner, soft blue-gray background
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF1F5F9)
val SurfaceContainerHigh = Color(0xFFE2E8F0)
val OnBackgroundLight = Color(0xFF0F172A)     // Modern Slate 900
val OnSurfaceLight = Color(0xFF1E293B)        // Modern Slate 800
val OutlineLight = Color(0xFFE2E8F0)          // Slate 200

val BackgroundDark = Color(0xFF020617)        // Very deep navy
val SurfaceDark = Color(0xFF0F172A)           // Slate 900
val SurfaceContainerLowDark = Color(0xFF1E293B)
val SurfaceContainerHighDark = Color(0xFF334155)
val OnBackgroundDark = Color(0xFFF8FAFC)
val OnSurfaceDark = Color(0xFFF1F5F9)
val OutlineDark = Color(0xFF1E293B)

// ===== GLASSMORPHISM & GRADIENTS =====
val GlassWhite = Color(0x33FFFFFF)
val GlassBlack = Color(0x33000000)

val PremiumGradientStart = Color(0xFF4A90D9)
val PremiumGradientEnd = Color(0xFF5BB8D9)

// ===== TEXT =====
val TextPrimary = Color(0xFF1A2332)           // Ganti ke dark blue-gray
val TextSecondary = Color(0xFF5A6C7D)         // Ganti ke gray-blue
val TextHint = Color(0xFF9AA6B5)              // Ganti ke soft hint