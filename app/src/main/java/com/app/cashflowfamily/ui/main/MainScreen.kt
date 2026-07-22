package com.app.cashflowfamily.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.cashflowfamily.ui.family.FamilyManagementScreen
import com.app.cashflowfamily.ui.home.HomeScreen
import com.app.cashflowfamily.ui.navigation.BottomNavItem
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.ui.settings.SettingsScreen
import com.app.cashflowfamily.ui.transaction.HistoryScreen
import com.app.cashflowfamily.viewmodel.NotificationViewModel
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    rootNavController: NavController
) {
    // Nav controller khusus untuk bottom nav (nested)
    val bottomNavController = rememberNavController()

    // Inisialisasi NotificationViewModel untuk real-time listener
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    // ===== START REAL-TIME LISTENER SAAT MAIN SCREEN DIBUKA =====
    // Listener akan berjalan selama aplikasi di MainScreen
    LaunchedEffect(Unit) {
        notificationViewModel.startRealTimeListener()
    }

    // ===== STOP LISTENER SAAT MAIN SCREEN DITUTUP =====
    DisposableEffect(Unit) {
        onDispose {
            notificationViewModel.stopRealTimeListener()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    rootNavController = rootNavController,
                    bottomNavController = bottomNavController,
                    notificationViewModel = notificationViewModel  // Pass viewModel yang sama
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(rootNavController = rootNavController)
            }

            composable(Screen.Family.route) {
                FamilyManagementScreen(rootNavController = rootNavController)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(rootNavController = rootNavController)
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        BottomNavItem.items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up ke start destination biar back stack tidak bertumpuk
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}