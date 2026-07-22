package com.app.cashflowfamily.ui.recurring

import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.data.model.Categories
import com.app.cashflowfamily.data.model.RecurringFrequency
import com.app.cashflowfamily.ui.components.AmountTextField
import com.app.cashflowfamily.ui.components.CategoryDropdown
import com.app.cashflowfamily.ui.components.DatePickerField
import com.app.cashflowfamily.ui.components.TransactionTypeSelector
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.utils.Resource
import com.app.cashflowfamily.viewmodel.RecurringViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringScreen(
    navController: NavController,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val actionState by viewModel.actionState.collectAsState()
    val scrollState = rememberScrollState()

    // Form state
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(RecurringFrequency.MONTHLY.value) }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var hasEndDate by remember { mutableStateOf(false) }
    var endDate by remember { mutableLongStateOf(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Reset kategori saat type berubah
    LaunchedEffect(type) {
        category = ""
    }

    // Handle action result
    LaunchedEffect(actionState) {
        when (actionState) {
            is Resource.Success -> {
                Toast.makeText(context, "Recurring berhasil dibuat", Toast.LENGTH_SHORT).show()
                viewModel.resetActionState()
                navController.popBackStack()
            }
            is Resource.Error -> {
                errorMessage = (actionState as Resource.Error).message
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    // Extract day of month & day of week dari startDate
    val calendar = Calendar.getInstance().apply { timeInMillis = startDate }
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK).let {
        // Calendar.SUNDAY = 1, Calendar.MONDAY = 2
        // Kita mau: Senin = 1, Minggu = 7
        if (it == Calendar.SUNDAY) 7 else it - 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Transaksi Berulang",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tambah recurring baru",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(16.dp)
        ) {
            // Type Selector
            Text(
                text = "Jenis Transaksi",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TransactionTypeSelector(
                selectedType = type,
                onTypeSelected = { type = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text("Nama Recurring") },
                placeholder = { Text("Contoh: Gaji Bulanan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            AmountTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category
            CategoryDropdown(
                categories = Categories.getCategories(type),
                selectedCategory = category,
                onCategorySelected = {
                    category = it
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description (Optional)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency Selector
            Text(
                text = "Frekuensi",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FrequencySelector(
                selectedFrequency = frequency,
                onFrequencySelected = { frequency = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Start Date
            Text(
                text = "Tanggal Mulai",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DatePickerField(
                selectedDate = startDate,
                onDateSelected = { startDate = it }
            )

            // Info tanggal
            val freqInfo = when (RecurringFrequency.fromValue(frequency)) {
                RecurringFrequency.DAILY -> "Transaksi akan dibuat setiap hari mulai tanggal ini"
                RecurringFrequency.WEEKLY -> "Transaksi akan dibuat setiap minggu di hari yang sama"
                RecurringFrequency.MONTHLY -> "Transaksi akan dibuat tanggal $dayOfMonth setiap bulan"
            }
            Text(
                text = freqInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // End Date Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hasEndDate = !hasEndDate }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Checkbox(
                    checked = hasEndDate,
                    onCheckedChange = { hasEndDate = it }
                )
                Text(
                    text = "Berlaku sampai tanggal tertentu",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (hasEndDate) {
                Spacer(modifier = Modifier.height(8.dp))
                DatePickerField(
                    selectedDate = endDate,
                    onDateSelected = { endDate = it },
                    label = "Tanggal Berakhir"
                )
            }

            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    val amountValue = CurrencyFormatter.parseRupiah(amount)

                    when {
                        name.isBlank() -> errorMessage = "Nama recurring wajib diisi"
                        amountValue <= 0 -> errorMessage = "Jumlah harus lebih dari 0"
                        category.isBlank() -> errorMessage = "Pilih kategori"
                        hasEndDate && endDate < startDate ->
                            errorMessage = "Tanggal berakhir harus setelah tanggal mulai"
                        else -> {
                            viewModel.addRecurring(
                                name = name.trim(),
                                type = type,
                                amount = amountValue,
                                category = category,
                                description = description.trim(),
                                frequency = frequency,
                                dayOfMonth = dayOfMonth,
                                dayOfWeek = dayOfWeek,
                                startDate = startDate,
                                endDate = if (hasEndDate) endDate else null
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = actionState !is Resource.Loading
            ) {
                if (actionState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Simpan Recurring",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FrequencySelector(
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RecurringFrequency.entries.forEach { freq ->
            FrequencyButton(
                text = freq.label,
                isSelected = selectedFrequency == freq.value,
                onClick = { onFrequencySelected(freq.value) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FrequencyButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}