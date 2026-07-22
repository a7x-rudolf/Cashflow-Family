package com.app.cashflowfamily.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.biometricDataStore by preferencesDataStore(name = "biometric_preferences")

@Singleton
class BiometricPreferences @Inject constructor(
    private val context: Context
) {
    private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")

    val isBiometricEnabled: Flow<Boolean> = context.biometricDataStore.data.map { prefs ->
        prefs[BIOMETRIC_ENABLED_KEY] ?: false  // Default: disabled
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.biometricDataStore.edit { prefs ->
            prefs[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
}