package com.app.cashflowfamily.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.cashflowfamily.ui.components.UpdateDialog
import com.app.cashflowfamily.ui.family.FamilyManagementScreen
import com.app.cashflowfamily.ui.home.HomeScreen
import com.app.cashflowfamily.ui.navigation.BottomNavItem
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.ui.settings.SettingsScreen
import com.app.cashflowfamily.ui.transaction.HistoryScreen
import com.app.cashflowfamily.viewmodel.NotificationViewModel
import com.app.cashflowfamily.viewmodel.UpdateViewModel

@Composable
fun MainScreen(
    rootNavController: NavController
) {
    val bottomNavController = rememberNavController()
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    val updateViewModel: UpdateViewModel = viewModel()
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Cek update otomatis saat pertama kali dibuka.
    // silent = true -> tidak ada toast "tidak ada update", dan hanya dijalankan
    // sekali per sesi (lihat UpdateViewModel.hasAutoChecked), sehingga tidak
    // muncul berulang setiap kali berpindah laman / kembali ke Beranda.
    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdate(silent = true)
    }

    // Tampilkan dialog jika ada update
    LaunchedEffect(updateInfo) {
        if (updateInfo != null) {
            showUpdateDialog = true
        }
    }

    // Start real-time listener
    LaunchedEffect(Unit) {
        notificationViewModel.startRealTimeListener()
    }

    // Stop listener saat screen ditutup
    DisposableEffect(Unit) {
        onDispose {
            notificationViewModel.stopRealTimeListener()
        }
    }

    Scaffold(
        bottomBar = {
            FloatingBottomBar(
                navController = bottomNavController,
                onAddTransactionClick = {
                    rootNavController.navigate(Screen.AddTransaction.route)
                }
            )
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
                    notificationViewModel = notificationViewModel
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

    // Dialog Update
    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            updateInfo = updateInfo!!,
            onDismiss = {
                showUpdateDialog = false
                updateViewModel.clearUpdateInfo()
            },
            onDownloadStart = {
                // Optional: log analytics
            }
        )
    }
}

/**
 * Bottom navbar bergaya "notched/scalloped" — bar dengan cekungan melengkung
 * di tengah atas yang memberi ruang untuk tombol Tambah Transaksi mengambang
 * BENAR-BENAR terpisah (ada jarak/gap, tidak overlap ke permukaan bar),
 * dilengkapi label di bawah tiap ikon dan dot indicator kecil untuk item
 * yang aktif. Mengikuti referensi desain terbaru.
 */
@Composable
private fun FloatingBottomBar(
    navController: NavController,
    onAddTransactionClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = BottomNavItem.items
    val leftItems = items.take(2)
    val rightItems = items.drop(2)

    val barHeight = 65.dp
    val fabSize = 60.dp
    val cornerRadius = 28.dp
    // Radius cekungan sedikit lebih besar dari FAB supaya ada "bantalan"/gap
    // yang jelas di sekeliling FAB saat dia mengambang di atasnya.
    val notchRadius = fabSize / 2 + 10.dp

    val barShape = remember(cornerRadius, notchRadius) {
        NotchedNavBarShape(cornerRadius = cornerRadius, notchRadius = notchRadius)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // ===== Bar dengan cekungan di tengah =====
        Surface(
            shape = barShape,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                leftItems.forEach { item ->
                    NavIconButton(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { navigateTo(navController, item.route, currentRoute) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Ruang kosong di tengah untuk cekungan/FAB
                Box(modifier = Modifier.width(notchRadius * 2))

                rightItems.forEach { item ->
                    NavIconButton(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { navigateTo(navController, item.route, currentRoute) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ===== Tombol Tambah Transaksi — mengambang terpisah di atas cekungan =====
        Surface(
            onClick = onAddTransactionClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -(fabSize / 2) - 4.dp)
                .size(fabSize)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tambah Transaksi",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Shape custom untuk bar dengan cekungan melengkung ("scallop") di tengah
 * sisi atas, tempat FAB mengambang. Sudut-sudut lain tetap membulat penuh
 * seperti pill bar sebelumnya.
 */
private class NotchedNavBarShape(
    private val cornerRadius: Dp,
    private val notchRadius: Dp
) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val cornerPx = with(density) { cornerRadius.toPx() }
        val notchPx = with(density) { notchRadius.toPx() }
        val centerX = size.width / 2f

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, cornerPx)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, cornerPx * 2, cornerPx * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(centerX - notchPx * 1.5f, 0f)

            // Kurva cekungan turun-naik di sekitar FAB (bentuk melengkung/scallop)
            cubicTo(
                centerX - notchPx * 0.85f, 0f,
                centerX - notchPx, notchPx * 0.95f,
                centerX, notchPx * 0.95f
            )
            cubicTo(
                centerX + notchPx, notchPx * 0.95f,
                centerX + notchPx * 0.85f, 0f,
                centerX + notchPx * 1.5f, 0f
            )

            lineTo(size.width - cornerPx, 0f)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    size.width - cornerPx * 2, 0f, size.width, cornerPx * 2
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(size.width, size.height - cornerPx)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    size.width - cornerPx * 2,
                    size.height - cornerPx * 2,
                    size.width,
                    size.height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(cornerPx, size.height)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    0f, size.height - cornerPx * 2, cornerPx * 2, size.height
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(0f, cornerPx)
            close()
        }

        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

private fun navigateTo(navController: NavController, route: String, currentRoute: String?) {
    if (currentRoute != route) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
private fun NavIconButton(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            // Kontras ikon inactive dinaikkan (dari 0.5 alpha jadi 0.65) biar
            // tetap jelas & seimbang, tidak terlalu pudar.
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        },
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "navIconTint"
    )
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.94f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "navIconScale"
    )
    val dotAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "navDotAlpha"
    )

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier
                .size(23.dp)
                .scale(scale)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = tint,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(4.dp)
                .graphicsLayer(alpha = dotAlpha)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}