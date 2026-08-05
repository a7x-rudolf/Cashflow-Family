@file:Suppress("DEPRECATION", "AvoidDuplicateDependencies")

import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
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
        versionCode = 12
        versionName = "1.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // SECURITY/POLICY FIX (v1.1.1): Google Play melarang app yang
    // didistribusikan lewat Play Store untuk update dirinya sendiri lewat
    // mekanisme di luar Play Store (lihat UpdateChecker/UpdateManager).
    // Flavor "playstore" TIDAK menyertakan fitur itu sama sekali (kode +
    // permission REQUEST_INSTALL_PACKAGES ada di app/src/github/ saja).
    // Build untuk Play Store HARUS pakai flavor "playstore".
    flavorDimensions += "distribution"
    productFlavors {
        create("github") {
            dimension = "distribution"
            buildConfigField(
                "String",
                "INVITE_APK_DOWNLOAD_URL",
                "\"https://github.com/a7x-rudolf/Cashflow-Family/releases/latest/download/Cashflow.Family.apk\""
            )
        }
        create("playstore") {
            dimension = "distribution"
            // TODO: ganti placeholder ini dengan link Play Store asli
            // setelah app live (lihat panduan "yang-harus-dikerjakan-sendiri.md").
            buildConfigField(
                "String",
                "INVITE_APK_DOWNLOAD_URL",
                "\"https://play.google.com/store/apps/details?id=com.app.cashflowfamily\""
            )
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile =
                localProperties.getProperty("CASHFLOW_KEYSTORE_FILE") ?: "cashflow-family-keystore.jks"
            val keystorePassword =
                localProperties.getProperty("CASHFLOW_STORE_PASSWORD") ?: ""
            val keystoreAlias =
                localProperties.getProperty("CASHFLOW_KEY_ALIAS") ?: ""
            val keystoreKeyPassword =
                localProperties.getProperty("CASHFLOW_KEY_PASSWORD") ?: ""

            storeFile = file(keystoreFile)
            storePassword = keystorePassword
            keyAlias = keystoreAlias
            keyPassword = keystoreKeyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-DEBUG"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        lintConfig = file("lint.xml")
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
    // ===== BOM =====
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    // ===== CORE =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // ===== LIFECYCLE =====
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)

    // ===== COMPOSE =====
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.ui.text.google.fonts)

    // ===== NAVIGATION =====
    implementation(libs.androidx.navigation.compose)

    // ===== HILT =====
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // ===== WORKMANAGER + HILT =====
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // ===== FIREBASE =====
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.functions.ktx)

    // ===== COROUTINES =====
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // ===== CHART =====
    implementation(libs.ycharts)

    // ===== SPLASH SCREEN =====
    implementation(libs.androidx.core.splashscreen)

    // ===== DATASTORE =====
    implementation(libs.androidx.datastore.preferences)

    // ===== NETWORK =====
    implementation(libs.okhttp)
    implementation(libs.gson)

    // ===== BIOMETRIC =====
    implementation(libs.androidx.biometric)

    // ===== GOOGLE SIGN-IN / CREDENTIAL MANAGER =====
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // ===== IMAGE LOADING =====
    implementation(libs.coil.compose)

    // ===== UI/UX =====
    implementation(libs.lottie.compose)
    implementation(libs.compose.shimmer)

    // ===== TEST =====
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}