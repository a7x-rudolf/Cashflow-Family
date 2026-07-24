@file:Suppress("DEPRECATION", "AvoidDuplicateDependencies")
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Firebase
    id("com.google.gms.google-services")

    // Hilt
    id("com.google.dagger.hilt.android")

    // KSP
    id("com.google.devtools.ksp")
}

// ==============================================================
// LOAD KEYSTORE CREDENTIALS DARI local.properties
// File local.properties TIDAK di-commit ke Git untuk security
// ==============================================================
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.app.cashflowfamily"
    //noinspection GradleDependency
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.cashflowfamily"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 7
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = localProperties.getProperty("CASHFLOW_KEYSTORE_FILE") ?: "cashflow-family-keystore.jks"
            val keystorePassword = localProperties.getProperty("CASHFLOW_STORE_PASSWORD") ?: ""
            val keystoreAlias = localProperties.getProperty("CASHFLOW_KEY_ALIAS") ?: ""
            val keystoreKeyPassword = localProperties.getProperty("CASHFLOW_KEY_PASSWORD") ?: ""

            storeFile = file(keystoreFile)
            storePassword = keystorePassword
            keyAlias = keystoreAlias
            keyPassword = keystoreKeyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    // ===== CORE =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // ===== LIFECYCLE (SEMUA VERSI SAMA: 2.8.7) =====
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")

    // ===== JETPACK COMPOSE =====
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ===== COMPOSE EXTENDED =====
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // ===== NAVIGATION =====
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // ===== HILT (Dependency Injection) =====
    //noinspection UseTomlInstead,NewerVersionAvailable
    implementation("com.google.dagger:hilt-android:2.51.1")
    //noinspection UseTomlInstead,NewerVersionAvailable
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ===== WORKMANAGER + HILT =====
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.hilt:hilt-work:1.2.0")
    //noinspection UseTomlInstead,GradleDependency
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // ===== FIREBASE =====
    //noinspection UseTomlInstead,GradleDependency
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-auth-ktx")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-firestore-ktx")

    // ===== COROUTINES =====
    //noinspection UseTomlInstead,NewerVersionAvailable
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    //noinspection UseTomlInstead,NewerVersionAvailable
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // ===== CHART =====
    //noinspection UseTomlInstead
    implementation("co.yml:ycharts:2.1.0")

    // ===== SPLASH SCREEN =====
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ===== DATASTORE =====
    //noinspection UseTomlInstead,GradleDependency
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ===== OKHTTP (Network) =====
    //noinspection UseTomlInstead,NewerVersionAvailable
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ===== TEST =====
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ===== GSON (JSON parsing) =====
    //noinspection UseTomlInstead,NewerVersionAvailable
    implementation("com.google.code.gson:gson:2.11.0")

    // ===== BIOMETRIC =====
    //noinspection UseTomlInstead
    implementation("androidx.biometric:biometric:1.1.0")

    // ===== GOOGLE SIGN-IN (Credential Manager) =====
    //noinspection GradleDependency,UseTomlInstead
    implementation("androidx.credentials:credentials:1.3.0")
    //noinspection GradleDependency,GradleDependency,UseTomlInstead
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    //noinspection GradleDependency,UseTomlInstead
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Firebase Cloud Messaging (Push Notification)
    //noinspection UseTomlInstead,GradleDependency
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")

    // Coil - image loading (foto profil dari URL Google & Bitmap hasil upload manual)
    implementation("io.coil-kt:coil-compose:2.7.0")
}