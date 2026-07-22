package com.app.cashflowfamily.di

import android.content.Context
import com.app.cashflowfamily.data.preferences.ThemePreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences {
        return ThemePreferences(context)
    }

    @Provides
    @Singleton
    fun provideNotificationPreferences(
        @ApplicationContext context: Context
    ): com.app.cashflowfamily.data.preferences.NotificationPreferences {
        return com.app.cashflowfamily.data.preferences.NotificationPreferences(context)
    }

    @Provides
    @Singleton
    fun provideOnboardingPreferences(
        @ApplicationContext context: Context
    ): com.app.cashflowfamily.data.preferences.OnboardingPreferences {
        return com.app.cashflowfamily.data.preferences.OnboardingPreferences(context)
    }

    @Provides
    @Singleton
    fun provideBiometricPreferences(
        @ApplicationContext context: Context
    ): com.app.cashflowfamily.data.preferences.BiometricPreferences {
        return com.app.cashflowfamily.data.preferences.BiometricPreferences(context)
    }
}