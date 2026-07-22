package com.app.cashflowfamily.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cashflowfamily.data.preferences.NotificationPreferences
import com.app.cashflowfamily.utils.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationPreferences: NotificationPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isDailyReminderEnabled: StateFlow<Boolean> = notificationPreferences.isDailyReminderEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isBudgetWarningEnabled: StateFlow<Boolean> = notificationPreferences.isBudgetWarningEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isFamilyActivityEnabled: StateFlow<Boolean> = notificationPreferences.isFamilyActivityEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.setDailyReminderEnabled(enabled)

            if (enabled) {
                NotificationScheduler.scheduleDailyReminder(context)
            } else {
                NotificationScheduler.cancelDailyReminder(context)
            }
        }
    }

    fun setBudgetWarningEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.setBudgetWarningEnabled(enabled)
        }
    }

    fun setFamilyActivityEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferences.setFamilyActivityEnabled(enabled)
        }
    }
}