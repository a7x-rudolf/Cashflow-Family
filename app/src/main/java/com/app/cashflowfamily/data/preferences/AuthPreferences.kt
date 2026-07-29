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

private val Context.authDataStore by preferencesDataStore(name = "auth_preferences")

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
    private val SAVED_EMAIL_KEY = stringPreferencesKey("saved_email")

    val isRememberMeEnabled: Flow<Boolean> = context.authDataStore.data.map { prefs ->
        prefs[REMEMBER_ME_KEY] ?: false
    }

    val savedEmail: Flow<String> = context.authDataStore.data.map { prefs ->
        prefs[SAVED_EMAIL_KEY] ?: ""
    }

    suspend fun getSavedEmailOnce(): String {
        return context.authDataStore.data.map { it[SAVED_EMAIL_KEY] ?: "" }.first()
    }

    suspend fun getRememberMeOnce(): Boolean {
        return context.authDataStore.data.map { it[REMEMBER_ME_KEY] ?: false }.first()
    }

    // Simpan preferensi "Ingat Saya". Kalau dimatikan, email yang tersimpan ikut dihapus.
    suspend fun saveRememberMe(enabled: Boolean, email: String) {
        context.authDataStore.edit { prefs ->
            prefs[REMEMBER_ME_KEY] = enabled
            if (enabled) {
                prefs[SAVED_EMAIL_KEY] = email
            } else {
                prefs[SAVED_EMAIL_KEY] = ""
            }
        }
    }
}