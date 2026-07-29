package com.app.cashflowfamily.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        label = "Beranda",
        iconFilled = Icons.Filled.Home,
        iconOutlined = Icons.Outlined.Home
    )

    object History : BottomNavItem(
        route = Screen.History.route,
        label = "Riwayat",
        iconFilled = Icons.Filled.History,
        iconOutlined = Icons.Outlined.History
    )

    // ===== BARU — Event menggantikan Family =====
    object Event : BottomNavItem(
        route = Screen.EventList.route,
        label = "Event",
        iconFilled = Icons.Filled.Celebration,
        iconOutlined = Icons.Outlined.Celebration
    )

    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        label = "Setelan",
        iconFilled = Icons.Filled.Settings,
        iconOutlined = Icons.Outlined.Settings
    )

    companion object {
        // Home, History | [FAB] | Event, Settings
        val items = listOf(Home, History, Event, Settings)
    }
}