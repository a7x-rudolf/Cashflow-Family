package com.app.cashflowfamily.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Group
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

    object Family : BottomNavItem(
        route = Screen.Family.route,
        label = "Keluarga",
        iconFilled = Icons.Filled.Group,
        iconOutlined = Icons.Outlined.Group
    )

    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        label = "Setelan",
        iconFilled = Icons.Filled.Settings,
        iconOutlined = Icons.Outlined.Settings
    )

    companion object {
        val items = listOf(Home, History, Family, Settings)
    }
}