@file:Suppress("DEPRECATION")

package com.app.cashflowfamily.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.statusBarColor = colorScheme.primary.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb() // Tambahkan navigation bar

                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme // Tambahkan untuk navigation bar
            } else {
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.primary.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = colorScheme.surface.toArgb()

                window.decorView.systemUiVisibility = if (!darkTheme) {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    View.SYSTEM_UI_FLAG_VISIBLE
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CashflowTypography,
        content = content
    )
}