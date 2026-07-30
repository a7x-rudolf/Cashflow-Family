package com.app.cashflowfamily.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.graphics.Color
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
import com.app.cashflowfamily.ui.components.HomeInsightWaveCard
import com.app.cashflowfamily.ui.components.TransactionItem
import com.app.cashflowfamily.ui.components.UserAvatar
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.utils.DateFormatter
import com.app.cashflowfamily.utils.HomeInsightHelper
import com.app.cashflowfamily.viewmodel.HomeViewModel
import com.app.cashflowfamily.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    rootNavController: NavController,
    bottomNavController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    recurringViewModel: com.app.cashflowfamily.viewmodel.RecurringViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()

    var selectedPageIndex by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(uiState.monthDataList.size) {
        if (uiState.monthDataList.isNotEmpty() && selectedPageIndex == 0) {
            selectedPageIndex = uiState.monthDataList.size - 1
        }
    }

    val currentMonthData = uiState.monthDataList.getOrNull(selectedPageIndex)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.refresh()
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
                val currentTransactions = currentMonthData?.transactions ?: emptyList()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        BalanceCardPager(
                            monthDataList = uiState.monthDataList,
                            onPageChanged = { newPage ->
                                selectedPageIndex = newPage
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        QuickActionBar(
                            onBudgetClick = { rootNavController.navigate(Screen.Budget.route) },
                            onRecurringClick = { rootNavController.navigate(Screen.RecurringList.route) },
                            onAnalyticsClick = { rootNavController.navigate(Screen.Analytics.route) },
                            onEventsClick = { 
                                bottomNavController.navigate(Screen.EventList.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    currentMonthData?.let { monthData ->
                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            val (dailyIncome, dailyExpense) = remember(monthData) {
                                HomeInsightHelper.dailyIncomeExpenseSeries(monthData)
                            }
                            val insight = remember(monthData) {
                                HomeInsightHelper.primaryInsight(monthData)
                            }

                            HomeInsightWaveCard(
                                dailyIncome = dailyIncome,
                                dailyExpense = dailyExpense,
                                insightTitle = insight.title,
                                insightDescription = insight.description,
                                insightType = insight.type,
                                onClick = {
                                    rootNavController.navigate(Screen.Analytics.route)
                                }
                            )
                        }
                    }

                    item {
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
                                        text = buildString {
                                            append(DateFormatter.formatMonthYear(it.monthTimestamp))
                                            if (currentTransactions.isNotEmpty()) {
                                                append(" · ${currentTransactions.size} transaksi")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            if (currentTransactions.isNotEmpty()) {
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
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (currentTransactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
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
                        }
                    } else {
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
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionBar(
    onBudgetClick: () -> Unit,
    onRecurringClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onEventsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionItem(
            icon = Icons.Filled.Wallet,
            label = "Budget",
            onClick = onBudgetClick,
            color = Color(0xFF4A90D9)
        )
        QuickActionItem(
            icon = Icons.Filled.Receipt,
            label = "Rutin",
            onClick = onRecurringClick,
            color = Color(0xFF5BB8D9)
        )
        QuickActionItem(
            icon = Icons.Filled.Insights,
            label = "Analisis",
            onClick = onAnalyticsClick,
            color = Color(0xFF81C784)
        )
        QuickActionItem(
            icon = Icons.Filled.CalendarMonth,
            label = "Event",
            onClick = onEventsClick,
            color = Color(0xFFFBC02D)
        )
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}
