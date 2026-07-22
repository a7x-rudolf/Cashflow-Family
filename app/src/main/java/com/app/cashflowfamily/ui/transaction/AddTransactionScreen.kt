package com.app.cashflowfamily.ui.transaction

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.data.model.Categories
import com.app.cashflowfamily.ui.components.AmountTextField
import com.app.cashflowfamily.ui.components.CategoryDropdown
import com.app.cashflowfamily.ui.components.DatePickerField
import com.app.cashflowfamily.ui.components.TransactionTypeSelector
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.utils.Resource
import com.app.cashflowfamily.viewmodel.AddTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // State form
    var transactionType by remember { mutableStateOf("expense") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val saveState by viewModel.saveState.collectAsState()
    val scrollState = rememberScrollState()

    // Reset kategori saat type berubah
    LaunchedEffect(transactionType) {
        category = ""
    }

    // Handle result
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    "Transaksi berhasil disimpan",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetState()
                navController.popBackStack()
            }
            is Resource.Error -> {
                errorMessage = state.message
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
                            text = "Tambah Transaksi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Catat pemasukan atau pengeluaran",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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

                // Save Button
                Button(
                    onClick = {
                        // Validasi
                        val amountValue = CurrencyFormatter.parseRupiah(amount)

                        when {
                            amount.isBlank() || amountValue <= 0 -> {
                                errorMessage = "Jumlah harus diisi dan lebih dari 0"
                            }
                            category.isBlank() -> {
                                errorMessage = "Pilih kategori terlebih dahulu"
                            }
                            else -> {
                                viewModel.saveTransaction(
                                    type = transactionType,
                                    amount = amountValue,
                                    category = category,
                                    description = description.trim(),
                                    date = selectedDate
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = saveState !is Resource.Loading
                ) {
                    if (saveState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Simpan Transaksi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}