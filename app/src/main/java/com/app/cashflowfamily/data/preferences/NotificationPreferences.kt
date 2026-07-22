package com.app.cashflowfamily.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore by preferencesDataStore(name = "notification_preferences")

@Singleton
class NotificationPreferences @Inject constructor(
    private val context: Context
) {
    private val DAILY_REMINDER_KEY = booleanPreferencesKey("daily_reminder_enabled")
    private val BUDGET_WARNING_KEY = booleanPreferencesKey("budget_warning_enabled")
    private val FAMILY_ACTIVITY_KEY = booleanPreferencesKey("family_activity_enabled")  // BARU

    // Daily reminder toggle
    val isDailyReminderEnabled: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[DAILY_REMINDER_KEY] ?: true
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[DAILY_REMINDER_KEY] = enabled
        }
    }

    // Budget warning toggle
    val isBudgetWarningEnabled: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[BUDGET_WARNING_KEY] ?: true
    }

    suspend fun setBudgetWarningEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[BUDGET_WARNING_KEY] = enabled
        }
    }

    // Family activity toggle (BARU)
    val isFamilyActivityEnabled: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[FAMILY_ACTIVITY_KEY] ?: true
    }

    suspend fun setFamilyActivityEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[FAMILY_ACTIVITY_KEY] = enabled
        }
    }
}