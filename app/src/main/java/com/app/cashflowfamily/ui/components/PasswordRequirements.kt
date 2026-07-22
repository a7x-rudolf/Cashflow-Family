package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PasswordRequirements(
    password: String,
    modifier: Modifier = Modifier
) {
    val requirements = listOf(
        "Minimal 8 karakter" to (password.length >= 8),
        "Mengandung huruf besar (A-Z)" to password.any { it.isUpperCase() },
        "Mengandung huruf kecil (a-z)" to password.any { it.isLowerCase() },
        "Mengandung angka (0-9)" to password.any { it.isDigit() }
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Aturan Password:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        requirements.forEach { (text, isValid) ->
            RequirementItem(text = text, isValid = isValid)
        }
    }
}

@Composable
private fun RequirementItem(
    text: String,
    isValid: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isValid) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (isValid) {
                Color(0xFF43A047) // Hijau
            } else {
                Color(0xFF9E9E9E) // Abu-abu
            },
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isValid) {
                Color(0xFF43A047)
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            }
        )
    }
}