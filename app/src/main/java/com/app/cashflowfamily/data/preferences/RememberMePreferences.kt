package com.app.cashflowfamily.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.rememberMeDataStore by preferencesDataStore(name = "remember_me_preferences")

@Suppress("AddExplicitTargetToParameterAnnotation", "PrivatePropertyName")
@Singleton
class RememberMePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me_enabled")
    private val SAVED_EMAIL_KEY = stringPreferencesKey("saved_email")

    val isRememberMeEnabled: Flow<Boolean> = context.rememberMeDataStore.data.map { prefs ->
        prefs[REMEMBER_ME_KEY] ?: false
    }

    val savedEmail: Flow<String> = context.rememberMeDataStore.data.map { prefs ->
        prefs[SAVED_EMAIL_KEY] ?: ""
    }

    suspend fun saveCredentials(email: String) {
        context.rememberMeDataStore.edit { prefs ->
            prefs[REMEMBER_ME_KEY] = true
            prefs[SAVED_EMAIL_KEY] = email
        }
    }

    suspend fun clearCredentials() {
        context.rememberMeDataStore.edit { prefs ->
            prefs[REMEMBER_ME_KEY] = false
            prefs[SAVED_EMAIL_KEY] = ""
        }
    }

    suspend fun getSavedEmailSync(): String {
        return savedEmail.first()
    }

    suspend fun isRememberMeEnabledSync(): Boolean {
        return isRememberMeEnabled.first()
    }
}