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
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Izinkan angka dan satu pemisah desimal (koma untuk ID)
            val filtered = input.filter { it.isDigit() || it == ',' }
            
            // Pastikan hanya ada satu koma
            val firstCommaIndex = filtered.indexOf(',')
            val cleaned = if (firstCommaIndex != -1) {
                val beforeComma = filtered.substring(0, firstCommaIndex + 1)
                val afterComma = filtered.substring(firstCommaIndex + 1).replace(",", "")
                beforeComma + afterComma
            } else {
                filtered
            }

            if (cleaned.isEmpty()) {
                onValueChange("")
            } else {
                // Format bagian sebelum koma dengan pemisah ribuan
                val parts = cleaned.split(",")
                val integerPart = parts[0].toLongOrNull() ?: 0L
                val formattedInteger = NumberFormat.getNumberInstance(Locale("id", "ID"))
                    .format(integerPart)
                
                val finalValue = if (parts.size > 1) {
                    "$formattedInteger,${parts[1]}"
                } else if (cleaned.endsWith(",")) {
                    "$formattedInteger,"
                } else {
                    formattedInteger
                }
                
                onValueChange(finalValue)
            }
        },
        label = { Text("Jumlah") },
        prefix = { Text("Rp  ") },
        placeholder = { Text("0") },
        keyboardOptions = keyboardOptions,
        singleLine = true,
        isError = isError,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            textAlign = TextAlign.Start,
            fontSize = 22.sp
        ),
        modifier = modifier.fillMaxWidth()
    )
}