package com.app.cashflowfamily.ui.event

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.data.model.EventType
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.viewmodel.EventDashboardViewModel
import com.app.cashflowfamily.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    navController: NavController,
    eventId: String,
    dashboardViewModel: EventDashboardViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val eventListState by eventViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(eventId) { dashboardViewModel.loadDashboard(eventId) }

    val event = uiState.event

    var name by remember(event) { mutableStateOf(event?.name ?: "") }
    var description by remember(event) { mutableStateOf(event?.description ?: "") }
    var selectedType by remember(event) { mutableStateOf(event?.type ?: EventType.WEDDING.name) }
    var totalBudgetText by remember(event) {
        mutableStateOf(
            if (event != null && event.totalBudget > 0)
                CurrencyFormatter.formatInput(event.totalBudget.toLong())
            else ""
        )
    }
    var eventDate by remember(event) { mutableLongStateOf(event?.eventDate ?: 0L) }
    var endDate by remember(event) { mutableLongStateOf(event?.endDate ?: 0L) }
    var showEventDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showValidationDialog by remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    LaunchedEffect(eventListState.error) {
        eventListState.error?.let {
            snackbarHostState.showSnackbar(it)
            eventViewModel.clearError()
        }
    }
    LaunchedEffect(eventListState.successMessage) {
        eventListState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            eventViewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Edit Event",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = event?.name ?: "Event",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (event == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val budgetValue = totalBudgetText.replace(".", "").replace(",", "")
            .filter { it.isDigit() }.toDoubleOrNull() ?: 0.0

        // Warning kalau new budget < total allocated
        val totalAllocated = uiState.categories.sumOf { it.allocatedBudget }
        val budgetLessThanAllocated = budgetValue > 0 && budgetValue < totalAllocated

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Tipe Event",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(EventType.entries.toTypedArray()) { type ->
                    FilterChip(
                        selected = selectedType == type.name,
                        onClick = { selectedType = type.name },
                        label = { Text(type.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Event *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isBlank()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            OutlinedTextField(
                value = totalBudgetText,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(15)
                    totalBudgetText = if (digits.isNotBlank()) {
                        CurrencyFormatter.formatInput(digits.toLongOrNull() ?: 0L)
                    } else ""
                },
                label = { Text("Total Budget *") },
                prefix = { Text("Rp ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = budgetLessThanAllocated,
                supportingText = if (budgetLessThanAllocated) {
                    {
                        Text(
                            "Budget lebih kecil dari total alokasi kategori (${CurrencyFormatter.format(totalAllocated)}). Kurangi alokasi kategori dulu.",
                            color = EventColors.ErrorText
                        )
                    }
                } else null
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showEventDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CalendarToday, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (eventDate > 0L) dateFormatter.format(Date(eventDate))
                        else "Tanggal Event"
                    )
                }
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f),
                    enabled = eventDate > 0L
                ) {
                    Icon(Icons.Filled.CalendarMonth, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (endDate > 0L) dateFormatter.format(Date(endDate))
                        else "Tgl Selesai"
                    )
                }
            }

            // Info spent yang tidak bisa diedit
            if (event.spentAmount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EventColors.InfoBg),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Sudah terpakai: ${CurrencyFormatter.format(event.spentAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EventColors.InfoText
                            )
                            Text(
                                "Angka ini akan otomatis update dari transaksi.",
                                style = MaterialTheme.typography.labelSmall,
                                color = EventColors.InfoText.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        showValidationDialog = "Nama event harus diisi"
                        return@Button
                    }
                    if (budgetValue <= 0) {
                        showValidationDialog = "Total budget harus lebih dari 0"
                        return@Button
                    }
                    if (budgetLessThanAllocated) {
                        showValidationDialog = "Total budget tidak boleh kurang dari total alokasi kategori"
                        return@Button
                    }
                    if (eventDate > 0 && endDate > 0 && endDate < eventDate) {
                        showValidationDialog = "Tanggal selesai tidak boleh sebelum tanggal event"
                        return@Button
                    }

                    val updatedEvent = event.copy(
                        name = name.trim(),
                        type = selectedType,
                        description = description.trim(),
                        totalBudget = budgetValue,
                        eventDate = eventDate,
                        endDate = endDate
                    )
                    eventViewModel.updateEvent(updatedEvent, onSuccess = {
                        navController.popBackStack()
                    })
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !eventListState.isLoading && name.isNotBlank()
            ) {
                if (eventListState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Simpan Perubahan")
                }
            }
        }
    }

    if (showEventDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = if (eventDate > 0L) eventDate else null
        )
        DatePickerDialog(
            onDismissRequest = { showEventDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { eventDate = it }
                    showEventDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEventDatePicker = false }) { Text("Batal") }
            }
        ) { DatePicker(state = state) }
    }

    if (showEndDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = if (endDate > 0L) endDate else null
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { endDate = it }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Batal") }
            }
        ) { DatePicker(state = state) }
    }

    showValidationDialog?.let { message ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showValidationDialog = null },
            title = { Text("Data Tidak Valid") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { showValidationDialog = null }) { Text("OK") }
            }
        )
    }
}