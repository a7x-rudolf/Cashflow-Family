package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Hanya terima angka
            val cleaned = input.filter { it.isDigit() }

            if (cleaned.isEmpty()) {
                onValueChange("")
            } else {
                // Format dengan titik ribuan
                val number = cleaned.toLongOrNull() ?: 0L
                val formatted = NumberFormat.getNumberInstance(Locale("in", "ID"))
                    .format(number)
                onValueChange(formatted)
            }
        },
        label = { Text("Jumlah") },
        prefix = { Text("Rp  ") },
        placeholder = { Text("0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = isError,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            textAlign = TextAlign.Start,
            fontSize = 22.sp
        ),
        modifier = modifier.fillMaxWidth()
    )
}