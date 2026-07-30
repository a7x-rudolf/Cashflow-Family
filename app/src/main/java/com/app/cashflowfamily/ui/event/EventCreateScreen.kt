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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.app.cashflowfamily.data.model.EventCategoryIcon
import com.app.cashflowfamily.data.model.EventType
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreateScreen(
    navController: NavController,
    viewModel: EventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentStep by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.WEDDING.name) }
    var eventDate by remember { mutableLongStateOf(0L) }
    var endDate by remember { mutableLongStateOf(0L) }
    var showEventDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var totalBudgetText by remember { mutableStateOf("") }

    val templates = remember(selectedType) {
        EventCategoryTemplates.getTemplates(selectedType)
    }
    val selectedIndices = remember(templates) {
        mutableStateListOf<Int>().apply { addAll(templates.indices) }
    }
    val categoryBudgets = remember(templates) { mutableStateMapOf<Int, String>() }
    val customCategories = remember { mutableStateListOf<CategoryTemplate>() }
    val customCategoryBudgets = remember { mutableStateMapOf<Int, String>() }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Buat Event",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (currentStep) {
                                0 -> "Langkah 1 dari 3 · Info Event"
                                1 -> "Langkah 2 dari 3 · Total Budget"
                                else -> "Langkah 3 dari 3 · Alokasi Kategori"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) currentStep-- else navController.popBackStack()
                    }) {
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 12.dp, bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { StepIndicator(currentStep) }

            if (currentStep == 0) {
                item {
                    Text("Tipe Event", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(EventType.entries.toTypedArray()) { type ->
                            FilterChip(
                                selected = selectedType == type.name,
                                onClick = { selectedType = type.name },
                                label = { Text(type.label) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Event") },
                        placeholder = { Text("Masukan Nama Event")},
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                }
                if (eventDate > 0L) {
                    val days = ((eventDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                    if (days > 0) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = EventColors.InfoBg),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Text(
                                    text = "H-$days",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EventColors.InfoText
                                )
                            }
                        }
                    }
                }
                item {
                    Button(
                        onClick = { if (name.isNotBlank()) currentStep = 1 },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank()
                    ) { Text("Lanjut") }
                }
            }

            if (currentStep == 1) {
                item {
                    Text(
                        "Berapa total budget untuk event ini?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Total keseluruhan dana yang disiapkan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val quickAmounts = listOf(5_000_000L, 10_000_000L, 25_000_000L, 50_000_000L, 100_000_000L)
                        items(quickAmounts) { amount ->
                            OutlinedButton(onClick = {
                                totalBudgetText = CurrencyFormatter.formatInput(amount)
                            }) {
                                Text(
                                    CurrencyFormatter.format(amount.toDouble()),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
                item {
                    Button(
                        onClick = {
                            if (parseBudgetText(totalBudgetText) > 0) currentStep = 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = parseBudgetText(totalBudgetText) > 0
                    ) { Text("Lanjut") }
                }
            }

            if (currentStep == 2) {
                item {
                    val totalBudget = parseBudgetText(totalBudgetText)
                    val totalAllocated = selectedIndices.sumOf {
                        parseBudgetText(categoryBudgets[it] ?: "")
                    } + customCategoryBudgets.values.sumOf { parseBudgetText(it) }
                    val unallocated = totalBudget - totalAllocated

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (unallocated < 0) EventColors.WarningBg
                            else EventColors.InfoBg
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Budget:")
                                Text(CurrencyFormatter.format(totalBudget), fontWeight = FontWeight.Bold)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Teralokasi:")
                                Text(CurrencyFormatter.format(totalAllocated))
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    if (unallocated >= 0) "Belum dialokasi:" else "Kelebihan alokasi:",
                                    fontWeight = FontWeight.Bold,
                                    color = if (unallocated < 0) EventColors.WarningText
                                    else EventColors.InfoText
                                )
                                Text(
                                    CurrencyFormatter.format(kotlin.math.abs(unallocated)),
                                    fontWeight = FontWeight.Bold,
                                    color = if (unallocated < 0) EventColors.WarningText
                                    else EventColors.InfoText
                                )
                            }
                        }
                    }
                }

                itemsIndexed(templates) { index, template ->
                    val isSelected = selectedIndices.contains(index)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIndices.add(index)
                                    else selectedIndices.remove(index)
                                }
                            )
                            Icon(
                                imageVector = template.iconKey.toEventCategoryIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                template.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = categoryBudgets[index] ?: "",
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }.take(15)
                                    categoryBudgets[index] = if (digits.isNotBlank()) {
                                        CurrencyFormatter.formatInput(digits.toLongOrNull() ?: 0L)
                                    } else ""
                                },
                                modifier = Modifier.width(140.dp),
                                enabled = isSelected,
                                prefix = { Text("Rp ", style = MaterialTheme.typography.labelSmall) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                itemsIndexed(customCategories.toList()) { index, custom ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                imageVector = custom.iconKey.toEventCategoryIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                custom.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = customCategoryBudgets[index] ?: "",
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }.take(15)
                                    customCategoryBudgets[index] = if (digits.isNotBlank()) {
                                        CurrencyFormatter.formatInput(digits.toLongOrNull() ?: 0L)
                                    } else ""
                                },
                                modifier = Modifier.width(140.dp),
                                prefix = { Text("Rp ", style = MaterialTheme.typography.labelSmall) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            IconButton(onClick = {
                                customCategories.removeAt(index)
                                customCategoryBudgets.remove(index)
                            }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Hapus kategori",
                                    tint = EventColors.ErrorText
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Tambah Kategori")
                    }
                }

                item {
                    Button(
                        onClick = {
                            val totalBudget = parseBudgetText(totalBudgetText)
                            val selected = selectedIndices.map { templates[it] }
                            val budgetMap = mutableMapOf<Int, Double>()

                            selectedIndices.forEachIndexed { i, originalIndex ->
                                budgetMap[i] = parseBudgetText(categoryBudgets[originalIndex] ?: "")
                            }

                            val allCategories = selected.toMutableList()
                            customCategories.forEachIndexed { cIndex, custom ->
                                allCategories.add(custom)
                                budgetMap[selected.size + cIndex] =
                                    parseBudgetText(customCategoryBudgets[cIndex] ?: "")
                            }

                            viewModel.createEvent(
                                name = name,
                                type = selectedType,
                                description = description,
                                totalBudget = totalBudget,
                                eventDate = eventDate,
                                endDate = endDate,
                                selectedTemplates = allCategories,
                                customBudgets = budgetMap,
                                onSuccess = { eventId ->
                                    navController.popBackStack()
                                    navController.navigate(Screen.EventDashboard.createRoute(eventId))
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && selectedIndices.isNotEmpty()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Buat Event")
                        }
                    }
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

    if (showAddCategoryDialog) {
        AddCustomCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { categoryName ->
                customCategories.add(
                    CategoryTemplate(
                        name = categoryName,
                        iconKey = EventCategoryIcon.OTHER.name,
                        colorHex = "#607D8B"
                    )
                )
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Info", "Budget", "Kategori").forEachIndexed { index, label ->
            val isActive = index <= currentStep
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStep) {
                            Icon(
                                Icons.Filled.Check, null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                "${index + 1}",
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun AddCustomCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategori Baru") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Kategori") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onAdd(name) },
                enabled = name.isNotBlank()
            ) { Text("Tambah") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

private fun parseBudgetText(text: String): Double {
    return CurrencyFormatter.parseRupiah(text)
}
