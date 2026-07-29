package com.app.cashflowfamily.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryLight,
    secondary = BlueSecondary,
    onSecondary = GreenOnPrimary,
    secondaryContainer = BlueSecondaryLight,
    tertiary = BlueSecondaryLight,  // Tambahkan untuk lebih modern
    onTertiary = BlueSecondaryDark,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = GreenPrimaryLight.copy(alpha = 0.1f), // Soft background
    outline = OutlineLight,
    outlineVariant = OutlineLight.copy(alpha = 0.5f),
    error = ExpenseRed,
    onError = GreenOnPrimary,
    inversePrimary = GreenPrimaryDark,
    inverseSurface = BackgroundDark,
    inverseOnSurface = OnBackgroundDark,
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = GreenPrimaryDark,
    primaryContainer = GreenPrimaryDark,
    secondary = BlueSecondaryLight,
    onSecondary = BlueSecondaryDark,
    secondaryContainer = BlueSecondaryDark,
    tertiary = BlueSecondaryLight,
    onTertiary = BlueSecondaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = GreenPrimaryDark.copy(alpha = 0.2f),
    outline = OutlineDark,
    outlineVariant = OutlineDark.copy(alpha = 0.5f),
    error = ExpenseRed,
    onError = GreenOnPrimary,
    inversePrimary = GreenPrimaryLight,
    inverseSurface = BackgroundLight,
    inverseOnSurface = OnBackgroundLight,
)

@Composable
fun CashflowFamilyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Edge-to-edge sudah diaktifkan sekali di MainActivity (enableEdgeToEdge()),
    // yang membuat status bar & navigation bar transparan. JANGAN set warna
    // manual di sini lagi (window.statusBarColor / navigationBarColor) --
    // selain API itu jadi no-op di targetSdk 35 (Android 15+), tiap layar
    // juga punya containerColor TopAppBar yang berbeda-beda (surface, primary,
    // transparent), jadi satu warna global tidak akan pernah cocok untuk semua.
    // Cukup atur kontras ikon (terang/gelap) mengikuti tema aplikasi --
    // background TopAppBar/Scaffold di tiap layar yang akan "mengisi" area
    // status bar secara otomatis lewat WindowInsets bawaan Compose.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CashflowTypography,
        content = content
    )
}

/**
 * Override sementara kontras ikon status bar untuk layar tertentu yang
 * TopAppBar-nya pakai warna solid gelap (mis. `MaterialTheme.colorScheme.primary`)
 * alih-alih `surface`/`background` bawaan tema. Panggil di awal body Composable
 * layar tsb, mis:
 *
 *   ForceStatusBarIcons(lightIcons = true) // ikon putih di atas header primary
 *
 * Otomatis kembali ke aturan tema global saat layar ini di-dispose (navigasi keluar).
 */
@Composable
fun ForceStatusBarIcons(lightIcons: Boolean, appIsDarkTheme: Boolean = isSystemInDarkTheme()) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(lightIcons, appIsDarkTheme) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !lightIcons
        onDispose {
            // Kembalikan ke aturan tema aplikasi (bukan tema sistem) saat layar
            // ini ditinggalkan -- app punya toggle Light/Dark/System sendiri.
            controller.isAppearanceLightStatusBars = !appIsDarkTheme
        }
    }
}