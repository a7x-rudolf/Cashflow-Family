package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.model.User
import com.app.cashflowfamily.data.preferences.RememberMePreferences
import com.app.cashflowfamily.data.repository.AuthRepository
import com.app.cashflowfamily.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rememberMePreferences: RememberMePreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<User>>(Resource.Idle)
    val authState: StateFlow<Resource<User>> = _authState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val resetPasswordState: StateFlow<Resource<Unit>> = _resetPasswordState.asStateFlow()

    // Cek apakah user sudah login
    fun isUserLoggedIn(): Boolean {
        return authRepository.getCurrentUser() != null
    }

    // Register
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading

            authRepository.register(name, email, password)
                .onSuccess { user ->
                    _authState.value = Resource.Success(user)
                }
                .onFailure { error ->
                    _authState.value = Resource.Error(
                        error.message ?: "Gagal mendaftar akun"
                    )
                }
        }
    }

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _authState.value = Resource.Loading

            authRepository.login(email, password)
                .onSuccess { user ->
                    // Save email kalau remember me dicentang
                    if (rememberMe) {
                        rememberMePreferences.saveCredentials(email)
                    } else {
                        rememberMePreferences.clearCredentials()
                    }

                    _authState.value = Resource.Success(user)
                }
                .onFailure { error ->
                    _authState.value = Resource.Error(
                        friendlyErrorMessage(error.message ?: "")
                    )
                }
        }
    }

    /**
     * Convert Firebase error message ke Bahasa Indonesia
     */
    private fun friendlyErrorMessage(errorMessage: String): String {
        return when {
            errorMessage.contains("credential is incorrect", ignoreCase = true) ||
                    errorMessage.contains("credential is malformed", ignoreCase = true) ||
                    errorMessage.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                    errorMessage.contains("wrong password", ignoreCase = true) ||
                    errorMessage.contains("has expired", ignoreCase = true) ->
                "Email atau password salah"

            errorMessage.contains("no user record", ignoreCase = true) ||
                    errorMessage.contains("user not found", ignoreCase = true) ->
                "Akun tidak ditemukan"

            errorMessage.contains("email address is badly formatted", ignoreCase = true) ->
                "Format email tidak valid"

            errorMessage.contains("network", ignoreCase = true) ->
                "Koneksi bermasalah, periksa internet Anda"

            errorMessage.contains("too-many-requests", ignoreCase = true) ->
                "Terlalu banyak percobaan, silakan coba lagi nanti"

            else -> "Login gagal, silakan coba lagi"
        }
    }

    /**
     * Get saved email untuk auto-fill
     */
    suspend fun getSavedEmail(): String {
        return rememberMePreferences.getSavedEmailSync()
    }

    suspend fun isRememberMeEnabled(): Boolean {
        return rememberMePreferences.isRememberMeEnabledSync()
    }

    // Login / Daftar dengan Google
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading

            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    _authState.value = Resource.Success(user)
                }
                .onFailure { error ->
                    _authState.value = Resource.Error(
                        error.message ?: "Gagal masuk dengan Google"
                    )
                }
        }
    }

    // Kirim email reset password
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading

            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _resetPasswordState.value = Resource.Success(Unit)
                }
                .onFailure { error ->
                    _resetPasswordState.value = Resource.Error(
                        error.message ?: "Gagal mengirim email reset password"
                    )
                }
        }
    }

    fun resetPasswordResetState() {
        _resetPasswordState.value = Resource.Idle
    }

    // Logout
    fun logout() {
        authRepository.logout()
        _authState.value = Resource.Idle
    }

    // Reset state (untuk kembali ke kondisi awal)
    fun resetState() {
        _authState.value = Resource.Idle
    }
}