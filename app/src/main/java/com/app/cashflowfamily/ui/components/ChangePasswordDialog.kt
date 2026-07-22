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
import com.app.cashflowfamily.utils.PasswordValidator

@Composable
fun ChangePasswordDialog(
    isLoading: Boolean,
    onConfirm: (currentPassword: String, newPassword: String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Ganti Password") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                PasswordTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        errorMessage = null
                    },
                    label = "Password Saat Ini",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                PasswordTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = "Password Baru",
                    modifier = Modifier.fillMaxWidth()
                )

                if (newPassword.isNotEmpty()) {
                    PasswordRequirements(
                        password = newPassword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = "Konfirmasi Password Baru",
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isLoading,
                onClick = {
                    when {
                        currentPassword.isBlank() -> {
                            errorMessage = "Password saat ini wajib diisi"
                        }
                        newPassword.isBlank() -> {
                            errorMessage = "Password baru wajib diisi"
                        }
                        currentPassword == newPassword -> {
                            errorMessage = "Password baru harus berbeda dari yang lama"
                        }
                        newPassword != confirmPassword -> {
                            errorMessage = "Konfirmasi password tidak cocok"
                        }
                        else -> {
                            val validation = PasswordValidator.validate(newPassword)
                            if (!validation.isValid) {
                                errorMessage = validation.errorMessage
                            } else {
                                onConfirm(currentPassword, newPassword)
                            }
                        }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    Text("Ganti")
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