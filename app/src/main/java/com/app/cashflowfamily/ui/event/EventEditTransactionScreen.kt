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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun EventEditTransactionScreen(
    navController: NavController,
    eventId: String,
    transactionId: String,
    viewModel: EventTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var oldAmount by remember { mutableDoubleStateOf(0.0) }
    var selectedCategoryId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var transactionDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    LaunchedEffect(eventId, transactionId) {
        viewModel.loadTransactionForEdit(eventId, transactionId)
    }

    LaunchedEffect(uiState.editingTransaction) {
        if (!initialized) {
            uiState.editingTransaction?.let { tx ->
                oldAmount = tx.amount
                selectedCategoryId = tx.categoryId
                name = tx.name
                amountText = if (tx.amount > 0) CurrencyFormatter.formatInput(tx.amount.toLong()) else ""
                transactionDate = tx.transactionDate
                initialized = true
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearEditingTransaction() }
    }

    val selectedCategory = uiState.categories.find { it.categoryId == selectedCategoryId }
    val amountValue = amountText.replace(".", "").replace(",", "")
        .filter { it.isDigit() }.toDoubleOrNull() ?: 0.0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Edit Pengeluaran",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ubah data pengeluaran",
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

        if (uiState.isLoading && !initialized) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.editingTransaction == null && !uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Transaksi tidak ditemukan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                                    Text(cat.name, fontWeight = FontWeight.Medium)
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
                isError = name.isBlank()
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
                onClick = {
                    if (name.isBlank()) {
                        showValidationDialog = "Deskripsi harus diisi"
                        return@Button
                    }
                    if (selectedCategoryId.isBlank()) {
                        showValidationDialog = "Kategori harus dipilih"
                        return@Button
                    }
                    if (amountValue <= 0.0) {
                        showValidationDialog = "Jumlah harus lebih dari Rp 0"
                        return@Button
                    }
                    val existing = uiState.editingTransaction ?: return@Button
                    val updated = existing.copy(
                        categoryId = selectedCategoryId,
                        name = name.trim(),
                        amount = amountValue,
                        transactionDate = transactionDate
                    )
                    viewModel.updateTransaction(
                        oldAmount = oldAmount,
                        transaction = updated,
                        onSuccess = { navController.popBackStack() }
                    )
                },
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
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
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
}