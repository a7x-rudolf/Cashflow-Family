package com.app.cashflowfamily.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.cashflowfamily.data.model.Categories
import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.ui.components.AmountTextField
import com.app.cashflowfamily.ui.components.CategoryDropdown
import com.app.cashflowfamily.ui.components.DatePickerField
import com.app.cashflowfamily.ui.components.TransactionTypeSelector
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.utils.Resource
import com.app.cashflowfamily.viewmodel.TransactionDetailViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EditTransactionForm(
    transaction: Transaction,
    viewModel: TransactionDetailViewModel,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    // Initial state dari data existing
    var transactionType by remember { mutableStateOf(transaction.type) }
    var amount by remember {
        mutableStateOf(
            NumberFormat.getNumberInstance(Locale("in", "ID"))
                .format(transaction.amount.toLong())
        )
    }
    var category by remember { mutableStateOf(transaction.category) }
    var description by remember { mutableStateOf(transaction.description) }
    var selectedDate by remember { mutableLongStateOf(transaction.date) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val updateState by viewModel.updateState.collectAsState()
    val scrollState = rememberScrollState()

    // Reset kategori kalau type berubah dan kategori tidak ada di list baru
    LaunchedEffect(transactionType) {
        val validCategories = Categories.getCategories(transactionType)
        if (!validCategories.contains(category)) {
            category = ""
        }
    }

    // Handle result
    LaunchedEffect(updateState) {
        when (updateState) {
            is Resource.Success -> {
                viewModel.resetUpdateState()
                onSaveSuccess()
            }
            is Resource.Error -> {
                errorMessage = (updateState as Resource.Error).message
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(16.dp)
    ) {
        // Type Selector
        Text(
            text = "Jenis Transaksi",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TransactionTypeSelector(
            selectedType = transactionType,
            onTypeSelected = {
                transactionType = it
                errorMessage = null
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Amount
        AmountTextField(
            value = amount,
            onValueChange = {
                amount = it
                errorMessage = null
            },
            isError = errorMessage != null && amount.isEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category
        CategoryDropdown(
            categories = Categories.getCategories(transactionType),
            selectedCategory = category,
            onCategorySelected = {
                category = it
                errorMessage = null
            },
            isError = errorMessage != null && category.isEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Deskripsi (Opsional)") },
            placeholder = { Text("Contoh: Makan siang di warung") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date
        DatePickerField(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        // Error Message
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = updateState !is Resource.Loading
            ) {
                Text("Batal")
            }

            Button(
                onClick = {
                    val amountValue = CurrencyFormatter.parseRupiah(amount)

                    when {
                        amount.isBlank() || amountValue <= 0 -> {
                            errorMessage = "Jumlah harus diisi dan lebih dari 0"
                        }
                        category.isBlank() -> {
                            errorMessage = "Pilih kategori terlebih dahulu"
                        }
                        else -> {
                            viewModel.updateTransaction(
                                type = transactionType,
                                amount = amountValue,
                                category = category,
                                description = description.trim(),
                                date = selectedDate
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = updateState !is Resource.Loading
            ) {
                if (updateState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Simpan",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}