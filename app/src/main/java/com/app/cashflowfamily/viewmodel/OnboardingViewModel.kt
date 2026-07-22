package com.app.cashflowfamily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.preferences.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    fun checkOnboardingStatus(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isCompleted = onboardingPreferences.isOnboardingCompleted.first()
            onResult(isCompleted)
        }
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted()
        }
    }
}