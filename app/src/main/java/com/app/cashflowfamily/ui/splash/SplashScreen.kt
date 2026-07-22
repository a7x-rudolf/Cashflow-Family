package com.app.cashflowfamily.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.cashflowfamily.R
import com.app.cashflowfamily.ui.navigation.Screen
import com.app.cashflowfamily.viewmodel.AuthViewModel
import com.app.cashflowfamily.viewmodel.BiometricViewModel
import com.app.cashflowfamily.viewmodel.FamilyViewModel
import com.app.cashflowfamily.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    familyViewModel: FamilyViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
    biometricViewModel: BiometricViewModel = hiltViewModel()
) {
    // Animasi
    val logoScale = remember { Animatable(0.5f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        logoAlpha.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic))
        logoScale.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic))
    }

    LaunchedEffect(key1 = true) {
        delay(300)
        textAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
    }

    // Logic Navigation - Semua di 1 LaunchedEffect
    LaunchedEffect(key1 = true) {
        android.util.Log.d("SplashScreen", "Starting navigation logic...")

        // Tunggu splash animation
        delay(2500)

        // STEP 1: Cek Onboarding
        val isOnboardingCompleted = checkOnboardingStatusSync(onboardingViewModel)
        android.util.Log.d("SplashScreen", "Onboarding completed: $isOnboardingCompleted")

        if (!isOnboardingCompleted) {
            android.util.Log.d("SplashScreen", "Navigate to Onboarding")
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // STEP 2: Cek Login Status
        val isLoggedIn = authViewModel.isUserLoggedIn()
        android.util.Log.d("SplashScreen", "Is logged in: $isLoggedIn")

        if (!isLoggedIn) {
            android.util.Log.d("SplashScreen", "Navigate to Login")
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // STEP 3: Cek Family Status
        val hasFamily = checkHasFamilySync(familyViewModel)
        android.util.Log.d("SplashScreen", "Has family: $hasFamily")

        if (!hasFamily) {
            android.util.Log.d("SplashScreen", "Navigate to CreateOrJoinFamily")
            navController.navigate(Screen.CreateOrJoinFamily.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // STEP 4: Cek Biometric Status
        val isBiometricEnabled = biometricViewModel.checkBiometricEnabledSync()
        android.util.Log.d("SplashScreen", "Biometric enabled: $isBiometricEnabled")

        // STEP 5: Navigate berdasarkan biometric
        val destination = if (isBiometricEnabled) {
            android.util.Log.d("SplashScreen", "Navigate to BiometricLock")
            Screen.BiometricLock.route
        } else {
            android.util.Log.d("SplashScreen", "Navigate to Main")
            Screen.Main.route
        }

        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // UI
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_cashflow),
                    contentDescription = "Cashflow Family Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer(
                            scaleX = logoScale.value,
                            scaleY = logoScale.value,
                            alpha = logoAlpha.value
                        )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Kelola Keuangan Keluarga",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer(alpha = textAlpha.value)
                )

                Spacer(modifier = Modifier.height(48.dp))

                LoadingDots(
                    modifier = Modifier.graphicsLayer(alpha = textAlpha.value)
                )
            }

            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFAAAAAA),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .graphicsLayer(alpha = textAlpha.value)
            )
        }
    }
}

// Helper functions untuk async check
private suspend fun checkOnboardingStatusSync(
    viewModel: OnboardingViewModel
): Boolean = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
    viewModel.checkOnboardingStatus { result ->
        if (continuation.isActive) {
            continuation.resumeWith(Result.success(result))
        }
    }
}

private suspend fun checkHasFamilySync(
    viewModel: FamilyViewModel
): Boolean = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
    viewModel.checkUserHasFamily { result ->
        if (continuation.isActive) {
            continuation.resumeWith(Result.success(result))
        }
    }
}

@Composable
private fun LoadingDots(
    modifier: Modifier = Modifier,
    dotColor: Color = Color(0xFF5EC5F5)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..2) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing, delayMillis = i * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$i"
            )

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

// Extension untuk animasi
private fun androidx.compose.ui.Modifier.scale(scale: Float): androidx.compose.ui.Modifier {
    return this.then(androidx.compose.ui.Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
}