package com.app.cashflowfamily.ui.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.data.model.Event
import com.app.cashflowfamily.data.model.EventCategory
import com.app.cashflowfamily.data.model.EventStatus
import com.app.cashflowfamily.data.model.EventType
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.ui.theme.BalanceCardGradientEnd
import com.app.cashflowfamily.ui.theme.BalanceCardGradientMid
import com.app.cashflowfamily.ui.theme.BalanceCardGradientStart
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.viewmodel.EventDashboardViewModel
import com.app.cashflowfamily.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDashboardScreen(
    navController: NavController,
    eventId: String,
    dashboardViewModel: EventDashboardViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) { dashboardViewModel.loadDashboard(eventId) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            dashboardViewModel.clearError()
        }
    }

    val event = uiState.event
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    // Fix bug 1.5: safe parse
    val eventType = remember(event?.type) { EventType.safeValueOf(event?.type) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = event?.name ?: "Event",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = eventType.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (event != null) {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {

                            DropdownMenuItem(
                                text = { Text("Edit Event") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(
                                        Screen.EventEdit.createRoute(eventId)
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Laporan") },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(
                                        Screen.EventReport.createRoute(eventId)
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.BarChart, null)
                                }
                            )

                            HorizontalDivider()

                            if (event.status == EventStatus.PLANNING.name) {
                                DropdownMenuItem(
                                    text = { Text("Mulai Event") },
                                    onClick = {
                                        showMenu = false
                                        eventViewModel.updateEventStatus(
                                            eventId,
                                            EventStatus.IN_PROGRESS
                                        )
                                        dashboardViewModel.loadDashboard(eventId)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            null,
                                            tint = EventColors.StatusInProgress
                                        )
                                    }
                                )
                            }

                            if (event.status == EventStatus.IN_PROGRESS.name) {
                                DropdownMenuItem(
                                    text = { Text("Selesaikan") },
                                    onClick = {
                                        showMenu = false
                                        eventViewModel.updateEventStatus(
                                            eventId,
                                            EventStatus.COMPLETED
                                        )
                                        dashboardViewModel.loadDashboard(eventId)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            null,
                                            tint = EventColors.StatusCompleted
                                        )
                                    }
                                )
                            }

                            if (event.status == EventStatus.COMPLETED.name) {
                                DropdownMenuItem(
                                    text = { Text("Arsipkan") },
                                    onClick = {
                                        showMenu = false
                                        eventViewModel.updateEventStatus(
                                            eventId,
                                            EventStatus.ARCHIVED
                                        )
                                        dashboardViewModel.loadDashboard(eventId)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Archive,
                                            null,
                                            tint = EventColors.StatusArchived
                                        )
                                    }
                                )
                            }

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Hapus Event",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirm = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (event != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        navController.navigate(
                            Screen.EventAddTransaction.createRoute(eventId)
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Pengeluaran") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && event == null) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        if (event == null) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Event tidak ditemukan")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 12.dp, bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { BudgetOverviewCard(event) }

            if (event.eventDate > 0L) {
                item { CountdownCard(event, dateFormatter) }
            }

            if (uiState.overBudgetCategories.isNotEmpty()) {
                item {
                    AlertCard(
                        title = "Over Budget!",
                        subtitle = "${uiState.overBudgetCategories.size} kategori melebihi budget",
                        categories = uiState.overBudgetCategories,
                        isError = true
                    )
                }
            }
            if (uiState.warningCategories.isNotEmpty()) {
                item {
                    AlertCard(
                        title = "Peringatan Budget",
                        subtitle = "${uiState.warningCategories.size} kategori > 80%",
                        categories = uiState.warningCategories,
                        isError = false
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Filled.SwapHoriz,
                        label = "Transfer",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate(Screen.EventTransfer.createRoute(eventId))
                        }
                    )
                    QuickActionCard(
                        icon = Icons.Filled.Settings,
                        label = "Kelola",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate(Screen.EventBudget.createRoute(eventId))
                        }
                    )
                    QuickActionCard(
                        icon = Icons.Filled.BarChart,
                        label = "Laporan",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate(Screen.EventReport.createRoute(eventId))
                        }
                    )
                }
            }

            item {
                Text(
                    "Alokasi Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(uiState.categories, key = { it.categoryId }) { category ->
                CategoryBudgetCard(
                    category = category,
                    onClick = {
                        navController.navigate(
                            Screen.EventTransactions.createRoute(eventId, category.categoryId)
                        )
                    }
                )
            }

            if (uiState.recentTransactions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Transaksi Terakhir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = {
                            navController.navigate(Screen.EventTransactions.createRoute(eventId))
                        }) { Text("Lihat Semua") }
                    }
                }

                items(uiState.recentTransactions, key = { it.transactionId }) { tx ->
                    val category = uiState.categories.find { it.categoryId == tx.categoryId }
                    Card(
                        onClick = {
                            navController.navigate(
                                Screen.EventEditTransaction.createRoute(eventId, tx.transactionId)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 18.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = (category?.iconKey ?: "OTHER").toEventCategoryIcon(),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    tx.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${category?.name ?: ""} • ${dateFormatter.format(Date(tx.transactionDate))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "-${CurrencyFormatter.format(tx.amount)}",
                                color = EventColors.ErrorText,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Event?") },
            text = {
                Column {
                    Text("Event \"${event?.name}\" akan dihapus permanen bersama semua:")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• ${uiState.categories.size} kategori budget",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("• Semua transaksi", style = MaterialTheme.typography.bodySmall)
                    Text("• Semua riwayat transfer", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tindakan ini tidak bisa dibatalkan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EventColors.ErrorText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        eventViewModel.deleteEvent(eventId) {
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = EventColors.ErrorText)
                ) { Text("Hapus Permanen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
            }
        )
    }
}
private val HeroCardShape = RoundedCornerShape(20.dp)
@Composable
private fun BudgetOverviewCard(event: Event) {
    val percentage = event.budgetPercentage

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HeroCardShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        BalanceCardGradientStart,
                        BalanceCardGradientMid,
                        BalanceCardGradientEnd
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.10f)
                ),
                HeroCardShape
            )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Total Budget",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.80f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = CurrencyFormatter.format(event.totalBudget),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = {
                    (percentage / 100f)
                        .coerceIn(0.0, 1.0).toFloat()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = when {
                    event.isOverBudget -> EventColors.ErrorText
                    event.isWarningBudget -> EventColors.WarningText
                    else -> Color.White
                },
                trackColor = Color.White.copy(alpha = 0.22f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${percentage.toInt()}% digunakan",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                InfoColumn(
                    label = "Terpakai",
                    value = CurrencyFormatter.format(event.spentAmount),
                    color = Color.White
                )

                InfoColumn(
                    label = "Sisa",
                    value = CurrencyFormatter.format(event.remainingBudget),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    color: Color
) {
    Column {

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.75f)
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CountdownCard(event: Event, dateFormatter: SimpleDateFormat) {
    val days = event.daysUntilEvent
    val isPassed = event.isEventPassed
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isPassed) "Event Sudah Berlalu" else "Countdown",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isPassed) MaterialTheme.colorScheme.onSurfaceVariant
                    else EventColors.InfoText.copy(alpha = 0.75f)
                )
                val text = when {
                    days < 0 -> "Sudah lewat"
                    days == 0 -> "Hari ini!"
                    days == 1 -> "Besok!"
                    days < 30 -> "$days hari lagi"
                    days < 365 -> "${days / 30} bulan lagi"
                    else -> "${days / 365} tahun lagi"
                }
                Text(
                    text, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPassed) MaterialTheme.colorScheme.onSurfaceVariant
                    else EventColors.InfoText
                )
            }
            Text(
                dateFormatter.format(Date(event.eventDate)),
                style = MaterialTheme.typography.bodySmall,
                color = if (isPassed) MaterialTheme.colorScheme.onSurfaceVariant
                else EventColors.InfoText.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun AlertCard(
    title: String,
    subtitle: String,
    categories: List<EventCategory>,
    isError: Boolean
) {
    val bgColor = if (isError) EventColors.ErrorBg else EventColors.WarningBg
    val txtColor = if (isError) EventColors.ErrorText else EventColors.WarningText

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = txtColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(subtitle, color = txtColor.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                }
            }
            categories.forEach { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 48.dp,
                            top = 8.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(cat.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text(
                        "${cat.budgetPercentage.toInt()}%",
                        color = txtColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryBudgetCard(
    category: EventCategory,
    onClick: () -> Unit
) {
    val percentage = category.budgetPercentage
    val budgetColor = when {
        category.isOverBudget -> EventColors.ErrorText
        category.isWarningBudget -> EventColors.WarningText
        else -> EventColors.SuccessText
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.iconKey.toEventCategoryIcon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            category.name,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (category.isOverBudget) {
                            Card(
                                shape = RoundedCornerShape(50),
                                colors = CardDefaults.cardColors(
                                    containerColor = EventColors.ErrorBg
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Text(
                                    text = "OVER",
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EventColors.ErrorText
                                )
                            }
                        } else if (category.isWarningBudget) {
                            Card(
                                shape = RoundedCornerShape(50),
                                colors = CardDefaults.cardColors(
                                    containerColor = EventColors.WarningBg
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Text(
                                    text = "80%",
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EventColors.WarningText
                                )
                            }
                        }
                    }
                    Row {
                        Text(
                            CurrencyFormatter.format(category.spentAmount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            " / ${CurrencyFormatter.format(category.effectiveBudget)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))

            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (percentage / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = budgetColor,
                strokeCap = StrokeCap.Round
            )
            if (category.transferredIn > 0 || category.transferredOut > 0) {
                Spacer(Modifier.height(4.dp))
                Row {
                    if (category.transferredIn > 0) {
                        Text(
                            "↓ +${CurrencyFormatter.format(category.transferredIn)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EventColors.SuccessText
                        )
                    }
                    if (category.transferredIn > 0 && category.transferredOut > 0) {
                        Text(" • ", style = MaterialTheme.typography.labelSmall)
                    }
                    if (category.transferredOut > 0) {
                        Text(
                            "↑ -${CurrencyFormatter.format(category.transferredOut)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EventColors.ErrorText
                        )
                    }
                }
            }
        }
    }
}