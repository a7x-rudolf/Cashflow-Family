package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.utils.DateFormatter
import com.app.cashflowfamily.viewmodel.AdvancedFilter
import com.app.cashflowfamily.viewmodel.UserOption
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AdvancedFilterSheet(
    currentFilter: AdvancedFilter,
    availableCategories: List<String>,
    availableUsers: List<UserOption>,
    onApply: (AdvancedFilter) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    // Local state (jangan langsung update sampai user klik Apply)
    var startDate by remember { mutableStateOf(currentFilter.startDate) }
    var endDate by remember { mutableStateOf(currentFilter.endDate) }
    var minAmountText by remember {
        mutableStateOf(
            currentFilter.minAmount?.toLong()?.let {
                NumberFormat.getNumberInstance(Locale("in", "ID")).format(it)
            } ?: ""
        )
    }
    var maxAmountText by remember {
        mutableStateOf(
            currentFilter.maxAmount?.toLong()?.let {
                NumberFormat.getNumberInstance(Locale("in", "ID")).format(it)
            } ?: ""
        )
    }
    var selectedCategories by remember { mutableStateOf(currentFilter.selectedCategories) }
    var selectedUserIds by remember { mutableStateOf(currentFilter.selectedUserIds) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Lanjutan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        startDate = null
                        endDate = null
                        minAmountText = ""
                        maxAmountText = ""
                        selectedCategories = emptySet()
                        selectedUserIds = emptySet()
                        onReset()
                    }
                ) {
                    Text("Reset", color = Color(0xFFE53935))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Range Section
            FilterSectionTitle("Rentang Tanggal")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePickerField(
                    selectedDate = startDate ?: System.currentTimeMillis(),
                    onDateSelected = { startDate = it },
                    label = "Dari",
                    modifier = Modifier.weight(1f)
                )

                DatePickerField(
                    selectedDate = endDate ?: System.currentTimeMillis(),
                    onDateSelected = { endDate = it },
                    label = "Sampai",
                    modifier = Modifier.weight(1f)
                )
            }

            if (startDate != null || endDate != null) {
                TextButton(
                    onClick = {
                        startDate = null
                        endDate = null
                    },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Hapus rentang tanggal",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Range Section
            FilterSectionTitle("Rentang Nominal")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = minAmountText,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() }
                        minAmountText = if (cleaned.isEmpty()) "" else {
                            NumberFormat.getNumberInstance(Locale("in", "ID"))
                                .format(cleaned.toLong())
                        }
                    },
                    label = { Text("Minimum") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = maxAmountText,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() }
                        maxAmountText = if (cleaned.isEmpty()) "" else {
                            NumberFormat.getNumberInstance(Locale("in", "ID"))
                                .format(cleaned.toLong())
                        }
                    },
                    label = { Text("Maksimum") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories Section
            if (availableCategories.isNotEmpty()) {
                FilterSectionTitle("Kategori (${selectedCategories.size} dipilih)")

                Spacer(modifier = Modifier.height(8.dp))

                MultiSelectChips(
                    items = availableCategories,
                    selectedItems = selectedCategories,
                    onItemToggle = { category ->
                        selectedCategories = if (category in selectedCategories) {
                            selectedCategories - category
                        } else {
                            selectedCategories + category
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Users Section
            if (availableUsers.isNotEmpty()) {
                FilterSectionTitle("Anggota Keluarga (${selectedUserIds.size} dipilih)")

                Spacer(modifier = Modifier.height(8.dp))

                MultiSelectChips(
                    items = availableUsers.map { it.userName },
                    selectedItems = availableUsers
                        .filter { it.userId in selectedUserIds }
                        .map { it.userName }
                        .toSet(),
                    onItemToggle = { userName ->
                        val user = availableUsers.find { it.userName == userName }
                        if (user != null) {
                            selectedUserIds = if (user.userId in selectedUserIds) {
                                selectedUserIds - user.userId
                            } else {
                                selectedUserIds + user.userId
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal")
                }

                Button(
                    onClick = {
                        val filter = AdvancedFilter(
                            startDate = startDate,
                            endDate = endDate,
                            minAmount = CurrencyFormatter.parseRupiah(minAmountText).takeIf { it > 0 },
                            maxAmount = CurrencyFormatter.parseRupiah(maxAmountText).takeIf { it > 0 },
                            selectedCategories = selectedCategories,
                            selectedUserIds = selectedUserIds
                        )
                        onApply(filter)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Terapkan", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FilterSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun MultiSelectChips(
    items: List<String>,
    selectedItems: Set<String>,
    onItemToggle: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isSelected = item in selectedItems

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onItemToggle(item) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}