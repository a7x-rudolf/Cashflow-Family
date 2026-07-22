package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.cashflowfamily.data.model.Categories
import com.app.cashflowfamily.utils.CurrencyFormatter
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetFormDialog(
    existingCategories: List<String> = emptyList(),  // Kategori yang sudah punya budget (di-filter)
    initialCategory: String = "",
    initialAmount: Double = 0.0,
    isEditMode: Boolean = false,
    isLoading: Boolean = false,
    onConfirm: (category: String, amount: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var amount by remember {
        mutableStateOf(
            if (initialAmount > 0) {
                NumberFormat.getNumberInstance(Locale("in", "ID"))
                    .format(initialAmount.toLong())
            } else ""
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Filter kategori yang belum punya budget (untuk mode Add)
    val availableCategories = if (isEditMode) {
        Categories.EXPENSE_CATEGORIES
    } else {
        Categories.EXPENSE_CATEGORIES.filter { it !in existingCategories }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(if (isEditMode) "Edit Budget" else "Tambah Budget")
        },
        text = {
            Column {
                if (!isEditMode) {
                    CategoryDropdown(
                        categories = availableCategories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = {
                            selectedCategory = it
                            errorMessage = null
                        },
                        label = "Kategori Pengeluaran",
                        isError = errorMessage != null && selectedCategory.isEmpty()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Text(
                        text = "Kategori: $initialCategory",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                AmountTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        errorMessage = null
                    },
                    isError = errorMessage != null && amount.isEmpty()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (!isEditMode && availableCategories.isEmpty()) {
                    Text(
                        text = "Semua kategori pengeluaran sudah memiliki budget",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isLoading && (isEditMode || availableCategories.isNotEmpty()),
                onClick = {
                    val amountValue = CurrencyFormatter.parseRupiah(amount)

                    when {
                        !isEditMode && selectedCategory.isBlank() -> {
                            errorMessage = "Pilih kategori terlebih dahulu"
                        }
                        amount.isBlank() || amountValue <= 0 -> {
                            errorMessage = "Nominal harus lebih dari 0"
                        }
                        else -> {
                            onConfirm(
                                if (isEditMode) initialCategory else selectedCategory,
                                amountValue
                            )
                        }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                } else {
                    Text(if (isEditMode) "Simpan" else "Tambah")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Batal")
            }
        }
    )
}