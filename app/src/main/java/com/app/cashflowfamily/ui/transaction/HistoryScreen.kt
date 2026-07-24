package com.app.cashflowfamily.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.app.cashflowfamily.ui.components.ActiveFilterChip
import com.app.cashflowfamily.ui.components.AdvancedFilterSheet
import com.app.cashflowfamily.ui.components.EmptyState
import com.app.cashflowfamily.ui.components.ExportDialog
import com.app.cashflowfamily.ui.components.ExportFormat
import com.app.cashflowfamily.ui.components.ExportResultDialog
import com.app.cashflowfamily.ui.components.FilterChips
import com.app.cashflowfamily.ui.components.MonthSelector
import com.app.cashflowfamily.ui.components.MonthSummaryCard
import com.app.cashflowfamily.ui.components.TransactionItem
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.utils.export.ExportHelper
import com.app.cashflowfamily.viewmodel.HistoryViewModel
import java.io.File
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    rootNavController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showExportDialog by remember { mutableStateOf(false) }
    var exportedFile by remember { mutableStateOf<File?>(null) }
    var exportedMimeType by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // Refresh data saat layar ini kembali terlihat (mis. setelah menambah transaksi
    // dari layar Tambah Transaksi lalu kembali ke tab Riwayat). Tanpa ini, data yang
    // ditampilkan adalah snapshot lama karena HistoryViewModel hanya memuat data
    // sekali saat pertama kali dibuat (ViewModel-nya bertahan selama tab masih ada
    // di back stack, jadi init { } tidak terpanggil ulang).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
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
                    Column(
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "Riwayat Transaksi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        val transactionCount = uiState.allTransactions.size
                        val currentMonthCount = uiState.filteredTransactions.size

                        Text(
                            text = when {
                                transactionCount == 0 -> "Belum ada transaksi"
                                currentMonthCount > 0 -> "$currentMonthCount transaksi ditampilkan"
                                else -> "$transactionCount transaksi total"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    // ===== SEARCH (dengan soft circle) =====
                    Box(
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Cari",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // ===== MENU LAINNYA (⋮) dengan soft circle =====
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { expanded = !expanded }) {
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
                            text = { Text("Filter Lanjutan") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.FilterAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                expanded = false
                                showFilterSheet = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Export Laporan") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.FileDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                expanded = false
                                if (uiState.filteredTransactions.isNotEmpty()) {
                                    showExportDialog = true
                                } else {
                                    Toast.makeText(context, "Tidak ada transaksi untuk diexport", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Reset Filter") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                expanded = false
                                viewModel.clearAllFilters()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // ===== SEARCH BAR (PROFESSIONAL, EXPANDABLE) =====
                if (showSearch) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 1.dp,
                                shape = RoundedCornerShape(14.dp),
                                clip = false
                            ),
                        placeholder = {
                            Text(
                                text = "Cari transaksi...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ===== ACTIVE FILTER INDICATOR =====
                if (uiState.advancedFilter.hasAnyFilter()) {
                    ActiveFilterChip(
                        activeCount = uiState.advancedFilter.activeFilterCount(),
                        onClearClick = { viewModel.clearAllFilters() },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // ===== MONTH SELECTOR =====
                if (uiState.advancedFilter.startDate == null && uiState.advancedFilter.endDate == null) {
                    MonthSelector(
                        selectedMonth = uiState.selectedMonth,
                        onPrevious = { viewModel.changeMonth(-1) },
                        onNext = { viewModel.changeMonth(1) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ===== MONTH SUMMARY =====
                MonthSummaryCard(
                    income = uiState.totalIncome,
                    expense = uiState.totalExpense
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ===== FILTER CHIPS =====
                FilterChips(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ===== CONTENT =====
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.filteredTransactions.isEmpty() -> {
                        EmptyState(
                            title = if (uiState.hasActiveAdvancedFilter) {
                                "Tidak Ada Hasil"
                            } else {
                                "Belum Ada Transaksi"
                            },
                            description = if (uiState.hasActiveAdvancedFilter) {
                                "Coba ubah filter atau kata kunci pencarian"
                            } else {
                                "Tidak ada transaksi di bulan ini"
                            },
                            icon = Icons.Filled.Receipt,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        TransactionList(
                            groupedTransactions = uiState.groupedTransactions,
                            onTransactionClick = { transactionId ->
                                rootNavController.navigate(
                                    Screen.TransactionDetail.createRoute(transactionId)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // ===== EXPORT DIALOG =====
    if (showExportDialog) {
        ExportDialog(
            onFormatSelected = { format ->
                showExportDialog = false

                Toast.makeText(context, "Sedang membuat file...", Toast.LENGTH_SHORT).show()

                when (format) {
                    ExportFormat.PDF -> {
                        val file = ExportHelper.exportToPdf(
                            context = context,
                            familyName = uiState.familyName.ifBlank { "Keluarga" },
                            monthTimestamp = uiState.selectedMonth,
                            transactions = uiState.filteredTransactions,
                            totalIncome = uiState.totalIncome,
                            totalExpense = uiState.totalExpense
                        )

                        if (file != null) {
                            exportedFile = file
                            exportedMimeType = "application/pdf"
                        } else {
                            Toast.makeText(context, "Gagal export PDF", Toast.LENGTH_SHORT).show()
                        }
                    }

                    ExportFormat.CSV -> {
                        val file = ExportHelper.exportToCsv(
                            context = context,
                            familyName = uiState.familyName.ifBlank { "Keluarga" },
                            monthTimestamp = uiState.selectedMonth,
                            transactions = uiState.filteredTransactions
                        )

                        if (file != null) {
                            exportedFile = file
                            exportedMimeType = "text/csv"
                        } else {
                            Toast.makeText(context, "Gagal export CSV", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onDismiss = { showExportDialog = false }
        )
    }

    // ===== EXPORT RESULT DIALOG =====
    exportedFile?.let { file ->
        ExportResultDialog(
            fileName = file.name,
            onShareClick = {
                ExportHelper.shareFile(context, file, exportedMimeType)
                exportedFile = null
            },
            onOpenClick = {
                ExportHelper.openFile(context, file, exportedMimeType)
                exportedFile = null
            },
            onDismiss = { exportedFile = null }
        )
    }

    // ===== ADVANCED FILTER SHEET =====
    if (showFilterSheet) {
        AdvancedFilterSheet(
            currentFilter = uiState.advancedFilter,
            availableCategories = uiState.availableCategories,
            availableUsers = uiState.availableUsers,
            onApply = { filter ->
                viewModel.setAdvancedFilter(filter)
                showFilterSheet = false
            },
            onReset = {
                viewModel.clearAllFilters()
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
private fun TransactionList(
    groupedTransactions: Map<String, List<com.app.cashflowfamily.data.model.Transaction>>,
    onTransactionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groupedTransactions.forEach { (dateLabel, transactions) ->
            item {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }

            items(
                items = transactions,
                key = { it.transactionId }
            ) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = {
                        onTransactionClick(transaction.transactionId)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}