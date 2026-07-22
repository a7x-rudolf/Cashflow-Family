package com.app.cashflowfamily.ui.budget

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.data.model.Budget
import com.app.cashflowfamily.ui.components.BudgetFormDialog
import com.app.cashflowfamily.ui.components.BudgetProgressCard
import com.app.cashflowfamily.ui.components.ConfirmationDialog
import com.app.cashflowfamily.ui.components.MonthSelector
import com.app.cashflowfamily.ui.components.TotalBudgetCard
import com.app.cashflowfamily.utils.Resource
import com.app.cashflowfamily.viewmodel.BudgetProgress
import com.app.cashflowfamily.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var deletingBudget by remember { mutableStateOf<Budget?>(null) }
    var menuBudget by remember { mutableStateOf<BudgetProgress?>(null) }

    // Handle action state
    LaunchedEffect(actionState) {
        when (actionState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    (actionState as Resource.Success).data,
                    Toast.LENGTH_SHORT
                ).show()
                showAddDialog = false
                editingBudget = null
                viewModel.resetActionState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (actionState as Resource.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Budget Bulanan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kelola anggaran keluarga",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Tambah Budget") }
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month Selector
                    item {
                        MonthSelector(
                            selectedMonth = uiState.selectedMonth,
                            onPrevious = { viewModel.changeMonth(-1) },
                            onNext = { viewModel.changeMonth(1) }
                        )
                    }

                    // Total Budget Card
                    if (uiState.budgets.isNotEmpty()) {
                        item {
                            TotalBudgetCard(
                                totalBudget = uiState.totalBudget,
                                totalSpent = uiState.totalSpent
                            )
                        }

                        // Section Header
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Budget per Kategori",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Budget Progress Cards
                        items(
                            items = uiState.budgetProgress,
                            key = { it.budget.budgetId }
                        ) { progress ->
                            BudgetProgressCard(
                                progress = progress,
                                onMenuClick = { menuBudget = progress }
                            )
                        }

                        // Bottom spacer for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    } else {
                        // Empty State
                        item {
                            EmptyBudgetState(
                                onAddClick = { showAddDialog = true }
                            )
                        }
                    }
                }
            }

            // Dropdown Menu untuk budget actions
            menuBudget?.let { progress ->
                Box(modifier = Modifier.padding(16.dp)) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { menuBudget = null }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Budget") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                editingBudget = progress.budget
                                menuBudget = null
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Hapus Budget",
                                    color = Color(0xFFE53935)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935)
                                )
                            },
                            onClick = {
                                deletingBudget = progress.budget
                                menuBudget = null
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Budget Dialog
    if (showAddDialog) {
        BudgetFormDialog(
            existingCategories = uiState.budgets.map { it.category },
            isEditMode = false,
            isLoading = actionState is Resource.Loading,
            onConfirm = { category, amount ->
                viewModel.addBudget(category, amount)
            },
            onDismiss = {
                if (actionState !is Resource.Loading) showAddDialog = false
            }
        )
    }

    // Edit Budget Dialog
    editingBudget?.let { budget ->
        BudgetFormDialog(
            initialCategory = budget.category,
            initialAmount = budget.amount,
            isEditMode = true,
            isLoading = actionState is Resource.Loading,
            onConfirm = { _, amount ->
                viewModel.updateBudget(budget, amount)
            },
            onDismiss = {
                if (actionState !is Resource.Loading) editingBudget = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingBudget?.let { budget ->
        ConfirmationDialog(
            title = "Hapus Budget?",
            message = "Budget untuk kategori \"${budget.category}\" akan dihapus. Data transaksi tidak akan terpengaruh.",
            confirmText = "Hapus",
            dismissText = "Batal",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteBudget(budget.budgetId)
                deletingBudget = null
            },
            onDismiss = { deletingBudget = null }
        )
    }
}

@Composable
private fun EmptyBudgetState(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Wallet,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Belum Ada Budget",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Set budget untuk mengontrol pengeluaran keluarga",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )
    }
}