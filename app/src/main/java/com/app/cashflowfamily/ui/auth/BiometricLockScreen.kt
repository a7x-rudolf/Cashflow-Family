package com.app.cashflowfamily.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.utils.BiometricHelper
import com.app.cashflowfamily.viewmodel.AuthViewModel

@Composable
fun BiometricLockScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = BiometricHelper.getActivity(context)

    // Auto-trigger biometric prompt saat screen dibuka
    LaunchedEffect(key1 = true) {
        activity?.let {
            triggerBiometricAuth(
                activity = it,
                onSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.BiometricLock.route) { inclusive = true }
                    }
                },
                onFallback = {
                    // Fallback to manual login
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.BiometricLock.route) { inclusive = true }
                    }
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Cashflow Family",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Aplikasi terkunci",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Gunakan fingerprint untuk melanjutkan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Tombol Autentikasi (untuk retry)
            Button(
                onClick = {
                    activity?.let {
                        triggerBiometricAuth(
                            activity = it,
                            onSuccess = {
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.BiometricLock.route) { inclusive = true }
                                }
                            },
                            onFallback = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.BiometricLock.route) { inclusive = true }
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "Autentikasi Fingerprint",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Login Manual
            TextButton(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.BiometricLock.route) { inclusive = true }
                    }
                }
            ) {
                Text(
                    text = "Login dengan Password",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun triggerBiometricAuth(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFallback: () -> Unit
) {
    BiometricHelper.showBiometricPrompt(
        activity = activity,
        title = "Autentikasi Diperlukan",
        subtitle = "Buka Cashflow Family dengan fingerprint",
        negativeButtonText = "Gunakan Password",
        onSuccess = onSuccess,
        onError = { errorCode, _ ->
            // Kalau user pilih "Gunakan Password" → fallback
            if (errorCode == androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                errorCode == androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED) {
                onFallback()
            }
            // Kalau error lain (misal 3x gagal), stay di lock screen, user bisa tap tombol retry
        },
        onFailed = {
            // Silent fail - user coba lagi
        }
    )
}