package com.app.cashflowfamily.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.app.cashflowfamily.ui.components.BalanceCardPager
import com.app.cashflowfamily.ui.components.EmptyState
import com.app.cashflowfamily.ui.components.TransactionItem
import com.app.cashflowfamily.ui.components.UserAvatar
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.utils.DateFormatter
import com.app.cashflowfamily.viewmodel.HomeViewModel
import com.app.cashflowfamily.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    rootNavController: NavController,
    bottomNavController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    recurringViewModel: com.app.cashflowfamily.viewmodel.RecurringViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()  // Akan di-pass dari MainScreen
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()

    // State: halaman yang aktif di pager
    var selectedPageIndex by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    // ===== LISTENER SUDAH DI-START DI MAINSCREEN =====
    // Tidak perlu start/stop listener di sini lagi

    // Update selectedPageIndex saat data ter-load pertama kali
    androidx.compose.runtime.LaunchedEffect(uiState.monthDataList.size) {
        if (uiState.monthDataList.isNotEmpty() && selectedPageIndex == 0) {
            selectedPageIndex = uiState.monthDataList.size - 1
        }
    }

    // Data bulan yang sedang aktif
    val currentMonthData = uiState.monthDataList.getOrNull(selectedPageIndex)

    // Auto-refresh saat kembali ke Home screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.refresh()
                // Unread count akan otomatis update via real-time listener

                // Process recurring transactions
                recurringViewModel.processDueRecurrings { count ->
                    if (count > 0) {
                        homeViewModel.refresh()
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UserAvatar(
                            name = uiState.user?.name ?: "?",
                            photoUrl = uiState.user?.photoUrl,
                            size = 36.dp,
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            textColor = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = com.app.cashflowfamily.utils.GreetingHelper
                                    .getGreetingWithName(uiState.user?.name ?: "User"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            ) {
                                Text(
                                    text = buildString {
                                        append(uiState.family?.familyName ?: "Keluarga")
                                        uiState.user?.role?.let { role ->
                                            append(" · ")
                                            append(if (role == "admin") "Admin" else "Member")
                                        }
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // ===== 1. NOTIFIKASI dengan Badge =====
                    Box(
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                rootNavController.navigate(Screen.Notification.route)
                            }
                        ) {
                            BadgedBox(
                                badge = {
                                    if (notificationUiState.unreadCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
                                            Text(
                                                text = if (notificationUiState.unreadCount > 99) "99+"
                                                else notificationUiState.unreadCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // ===== 2. TITIK TIGA (⋮) =====
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { expanded = !expanded }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Menu Lainnya",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Budget") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Wallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                expanded = false
                                rootNavController.navigate(Screen.Budget.route)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Insight") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Insights,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                expanded = false
                                rootNavController.navigate(Screen.Analytics.route)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Bantuan & Feedback") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Message,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                expanded = false
                                rootNavController.navigate(Screen.Feedback.route)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        // Tombol "Tambah Transaksi" dipindahkan ke bottom navigation bar
        // (lihat MainScreen.FloatingBottomBar) supaya tidak lagi menutupi
        // list transaksi di halaman Beranda.
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ===== FIXED AREA =====
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        BalanceCardPager(
                            monthDataList = uiState.monthDataList,
                            onPageChanged = { newPage ->
                                selectedPageIndex = newPage
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Transaksi",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                currentMonthData?.let {
                                    Text(
                                        text = DateFormatter.formatMonthYear(it.monthTimestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            if (currentMonthData?.transactions?.isNotEmpty() == true) {
                                TextButton(
                                    onClick = {
                                        bottomNavController.navigate(Screen.History.route) {
                                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                ) {
                                    Text("Lihat Semua")
                                }
                            }
                        }
                    }

                    // ===== SCROLLABLE AREA =====
                    val currentTransactions = currentMonthData?.transactions ?: emptyList()

                    if (currentTransactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = "Belum Ada Transaksi",
                                description = currentMonthData?.let {
                                    "Tidak ada transaksi di ${DateFormatter.formatMonthYear(it.monthTimestamp)}"
                                } ?: "Klik tombol + untuk menambah transaksi",
                                icon = Icons.Filled.Receipt
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = currentTransactions,
                                key = { it.transactionId }
                            ) { transaction ->
                                TransactionItem(
                                    transaction = transaction,
                                    onClick = {
                                        rootNavController.navigate(
                                            Screen.TransactionDetail.createRoute(transaction.transactionId)
                                        )
                                    }
                                )
                            }

                            item {
                                // Sedikit ruang di bawah list (FAB sudah dipindahkan ke bottom navbar)
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}