package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.preferences.BiometricPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BiometricViewModel @Inject constructor(
    private val biometricPreferences: BiometricPreferences
) : ViewModel() {

    val isBiometricEnabled: StateFlow<Boolean> = biometricPreferences.isBiometricEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            biometricPreferences.setBiometricEnabled(enabled)
        }
    }

    suspend fun checkBiometricEnabledSync(): Boolean {
        return biometricPreferences.isBiometricEnabled.first()
    }
}