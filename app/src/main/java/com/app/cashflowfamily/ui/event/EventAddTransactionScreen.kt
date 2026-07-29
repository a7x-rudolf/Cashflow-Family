package com.app.cashflowfamily.ui.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.viewmodel.EventTransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventAddTransactionScreen(
    navController: NavController,
    eventId: String,
    preselectedCategoryId: String = "",
    viewModel: EventTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedCategoryId by remember { mutableStateOf(preselectedCategoryId) }
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var transactionDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf<String?>(null) }
    var showOverBudgetConfirm by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    LaunchedEffect(eventId) { viewModel.loadTransactions(eventId) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.categories) {
        if (selectedCategoryId.isBlank() && uiState.categories.isNotEmpty()) {
            selectedCategoryId = uiState.categories.first().categoryId
        }
    }

    val selectedCategory = uiState.categories.find { it.categoryId == selectedCategoryId }
    val amountValue = amountText.replace(".", "").replace(",", "")
        .filter { it.isDigit() }.toDoubleOrNull() ?: 0.0

    fun submitTransaction() {
        viewModel.addTransaction(
            eventId = eventId,
            categoryId = selectedCategoryId,
            name = name,
            amount = amountValue,
            transactionDate = transactionDate,
            onSuccess = { navController.popBackStack() }
        )
    }

    fun onSubmitClicked() {
        if (name.isBlank()) {
            showValidationDialog = "Deskripsi pengeluaran harus diisi"
            return
        }
        if (selectedCategoryId.isBlank()) {
            showValidationDialog = "Pilih kategori terlebih dahulu"
            return
        }
        if (amountValue <= 0.0) {
            showValidationDialog = "Jumlah pengeluaran harus lebih dari Rp 0"
            return
        }

        if (selectedCategory != null) {
            val newTotal = selectedCategory.spentAmount + amountValue
            if (newTotal > selectedCategory.effectiveBudget) {
                showOverBudgetConfirm = true
                return
            }
        }
        submitTransaction()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tambah Pengeluaran",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Catat pengeluaran event",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        // Empty state kalau tidak ada kategori
        if (uiState.categories.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Category,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Belum ada kategori budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tambahkan kategori budget terlebih dahulu\ndi menu Kelola Budget",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Kembali", fontWeight = FontWeight.Bold)
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Pilih Kategori",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = selectedCategory?.let {
                        {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = it.iconKey.toEventCategoryIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.categories.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = cat.iconKey.toEventCategoryIcon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(cat.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            "Sisa: ${CurrencyFormatter.format(cat.remainingBudget)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (cat.isOverBudget)
                                                EventColors.ErrorText
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedCategoryId = cat.categoryId
                                expanded = false
                            }
                        )
                    }
                }
            }

            selectedCategory?.let { cat ->
                val (bgColor, txtColor) = when {
                    cat.isOverBudget -> EventColors.ErrorBg to EventColors.ErrorText
                    cat.isWarningBudget -> EventColors.WarningBg to EventColors.WarningText
                    else -> EventColors.InfoBg to EventColors.InfoText
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = BorderStroke(1.dp, txtColor.copy(alpha = 0.15f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Budget Kategori",
                                style = MaterialTheme.typography.labelSmall,
                                color = txtColor.copy(alpha = 0.75f)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                CurrencyFormatter.format(cat.effectiveBudget),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = txtColor
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Terpakai: ${CurrencyFormatter.format(cat.spentAmount)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = txtColor.copy(alpha = 0.75f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Sisa Budget",
                                style = MaterialTheme.typography.labelSmall,
                                color = txtColor.copy(alpha = 0.75f)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                CurrencyFormatter.format(cat.remainingBudget),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = txtColor
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Deskripsi *") },
                placeholder = { Text("Contoh: Beli Konsumsi") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isBlank() && amountText.isNotBlank()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(15)
                    amountText = if (digits.isNotBlank()) {
                        CurrencyFormatter.formatInput(digits.toLongOrNull() ?: 0L)
                    } else ""
                },
                label = { Text("Jumlah *") },
                prefix = { Text("Rp ") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amountText.isNotBlank() && amountValue <= 0.0,
                supportingText = if (amountText.isNotBlank() && amountValue <= 0.0) {
                    { Text("Jumlah harus lebih dari 0") }
                } else null
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.CalendarToday,
                    null,
                    Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    dateFormatter.format(Date(transactionDate)),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onSubmitClicked() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Simpan Pengeluaran", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = transactionDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { transactionDate = it }
                    showDatePicker = false
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) { DatePicker(state = state) }
    }

    showValidationDialog?.let { message ->
        AlertDialog(
            onDismissRequest = { showValidationDialog = null },
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Data Tidak Lengkap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showValidationDialog = null }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showOverBudgetConfirm && selectedCategory != null) {
        val newTotal = selectedCategory.spentAmount + amountValue
        val excess = newTotal - selectedCategory.effectiveBudget
        AlertDialog(
            onDismissRequest = { showOverBudgetConfirm = false },
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Melebihi Budget!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EventColors.ErrorText
                )
            },
            text = {
                Column {
                    Text(
                        "Pengeluaran ini akan membuat kategori \"${selectedCategory.name}\" melebihi budget.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = EventColors.ErrorBg,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Kelebihan: ${CurrencyFormatter.format(excess)}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            color = EventColors.ErrorText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Tetap lanjutkan?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOverBudgetConfirm = false
                        submitTransaction()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = EventColors.ErrorText)
                ) { Text("Ya, Simpan", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showOverBudgetConfirm = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}