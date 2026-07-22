# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ==============================================================
# GENERAL
# ==============================================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Keep Kotlin metadata (needed for reflection-based libraries like Gson/Firestore)
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$Companion { *; }

# ==============================================================
# APP DATA MODELS - SPESIFIK PER CLASS
# Firestore's toObject()/Gson's fromJson() rely on reflection
# ==============================================================
# Data Model Classes - specify each class explicitly
-keep class com.app.cashflowfamily.data.model.User {
    <fields>;
    <init>(...);
}
-keep class com.app.cashflowfamily.data.model.Family {
    <fields>;
    <init>(...);
}
-keep class com.app.cashflowfamily.data.model.Transaction {
    <fields>;
    <init>(...);
}
-keep class com.app.cashflowfamily.data.model.Budget {
    <fields>;
    <init>(...);
}
-keep class com.app.cashflowfamily.data.model.RecurringTransaction {
    <fields>;
    <init>(...);
}
-keep class com.app.cashflowfamily.data.model.Notification {
    <fields>;
    <init>(...);
}
-keep class com.app.cashflowfamily.data.model.Feedback {
    <fields>;
    <init>(...);
}
-keep class com.app.cashflowfamily.data.model.Categories { *; }

# Backup Data Classes (located in viewmodel package)
-keep class com.app.cashflowfamily.viewmodel.BackupData {
    <fields>;
    <init>(...);
}
-keep class com.app.cashflowfamily.viewmodel.RestoreResult {
    <fields>;
    <init>(...);
}

# Enum classes
-keep class com.app.cashflowfamily.data.model.RecurringFrequency { *; }
-keep class com.app.cashflowfamily.data.model.FeedbackType { *; }

# Keep PropertyName annotations
-keepclassmembers class com.app.cashflowfamily.data.model.** {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# ==============================================================
# FIREBASE (Auth, Firestore, Messaging)
# ==============================================================
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.**
-keepattributes *Annotation*

# Keep Firestore Field/Property annotations
-keepclassmembers class com.google.firebase.firestore.** {
    *;
}

# Firebase Cloud Messaging service
-keep class com.app.cashflowfamily.utils.FCMService { *; }

# ==============================================================
# GOOGLE SIGN-IN / CREDENTIAL MANAGER
# ==============================================================
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**

# ==============================================================
# GSON (used for local JSON backup/export)
# ==============================================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# ==============================================================
# HILT / DAGGER
# ==============================================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class **_HiltComponents$* { *; }
-keep class **_HiltModules$* { *; }
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
    @javax.inject.Inject <init>(...);
}
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ==============================================================
# WORKMANAGER + HILT-WORK
# ==============================================================
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class com.app.cashflowfamily.utils.worker.** { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }

# ==============================================================
# VIEWMODELS & REPOSITORIES
# ==============================================================
-keep class com.app.cashflowfamily.viewmodel.** {
    <init>(...);
    <fields>;
}
-keep class com.app.cashflowfamily.repository.** {
    <init>(...);
    <fields>;
}
-keep class com.app.cashflowfamily.data.repository.** {
    <init>(...);
    <fields>;
}

# ==============================================================
# BIOMETRIC
# ==============================================================
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ==============================================================
# COROUTINES
# ==============================================================
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**FlowKt

# ==============================================================
# YCHARTS (chart library)
# ==============================================================
-keep class co.yml.charts.** { *; }
-dontwarn co.yml.charts.**

# ==============================================================
# COMPOSE / NAVIGATION
# ==============================================================
-dontwarn androidx.compose.**
-keep class androidx.navigation.** { *; }

# ==============================================================
# PARCELABLE / ENUMS
# ==============================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ==============================================================
# ANDROIDX CORE
# ==============================================================
-keep class androidx.core.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }

# ==============================================================
# KOTLIN REFLECTION & SERIALIZATION
# ==============================================================
-keep class kotlin.reflect.** { *; }
-keep class kotlin.jvm.internal.** { *; }

# ==============================================================
# REMOVE LOGGING IN RELEASE (optional)
# ==============================================================
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}