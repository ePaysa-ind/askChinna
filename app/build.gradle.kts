// File: app/build.gradle.kts
// Copyright © 2025 askChinna
// Created: April 28, 2025
// Updated: May 4, 2025
// Version: 1.2
//
// Application‑level Gradle configuration, including BuildConfig fields for API keys,
// annotation processing via KAPT (Hilt & Glide) and kapt (Room), and plugin declarations.

import org.gradle.api.JavaVersion
import java.util.Locale
import java.util.Properties

plugins {
    // Android Application plugin (AGP)
    alias(libs.plugins.android.application)

    // Kotlin Android support
    alias(libs.plugins.kotlin.android)

    // Kotlin Compose tooling
    alias(libs.plugins.kotlin.compose)

    // Kotlin Annotation Processing (for Hilt & Glide)
    alias(libs.plugins.kotlin.kapt)

    // Dagger Hilt plugin
    alias(libs.plugins.hilt)

    // Google Services for Firebase (from version catalog)
    alias(libs.plugins.google.services)

    // Kotlin Parcelize
    kotlin("plugin.parcelize")

}

android {
    namespace   = "com.example.askchinna"
    compileSdk  = 35

    defaultConfig {
        applicationId         = "com.example.askchinna"
        minSdk                = 24 // Minimum SDK version
        targetSdk             = 35
        versionCode           = 1
        versionName           = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject API keys into BuildConfig
        buildConfigField(
            type="String",
            name="GEMINI_API_KEY",
            value="\"${getGeminiApiKey()}\""
        )
        buildConfigField(
            type="String",
            name="FIREBASE_API_KEY",
            value="\"${getFirebaseApiKey()}\""
        )

        // Privacy‑policy URL for manifest placeholders
        manifestPlaceholders["privacyPolicyUrl"] =
            "https://github.com/ePaysa-ind/askChinna/blob/master/PRIVACY_POLICY.md"
    }

    kapt {
        correctErrorTypes = true
        useBuildCache = true
    }

    buildTypes {
        release {
            // No code shrinking for now
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug build uses same BuildConfig fields
        }
    }

    // Java 11 compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // Enable Compose, ViewBinding, and BuildConfig
    buildFeatures {
        compose     = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // --- Core AndroidX ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- Jetpack Compose ---
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material)


    // --- Dagger Hilt ---
    implementation(libs.hilt.android)
    kapt        (libs.hilt.compiler)

    // --- JSON parsing ---
    implementation(libs.google.gson)

    // --- Firebase (BOM) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // --- Room (Local DB) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // kapt for Room’s annotation processing
    kapt(libs.androidx.room.compiler)

    // --- Google Gemini API client ---
    implementation(libs.generativeai)

    // --- UI & Utilities ---
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.services.location)
    implementation(libs.androidx.webkit)

    // --- Image loading (Glide) ---
    implementation(libs.glide)

    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    // Retrofit core
    implementation(libs.retrofit)
    // Retrofit GSON support
    implementation(libs.converter.gson)




    // --- Testing ---
    testImplementation       (libs.junit)
    testImplementation       (libs.mockk)
    testImplementation       (libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation      (libs.androidx.ui.tooling)
    debugImplementation      (libs.androidx.ui.test.manifest)
    testImplementation       (libs.androidx.arch.core.testing)
}

// --------------------------------------------------------------------------------
// Helper functions to read from `local.properties` or environment variables
// --------------------------------------------------------------------------------

/**
 * Load a property from local.properties, or fallback to an ENV var (uppercase).
 */
fun findLocalProperty(propName: String): String {
    val props     = Properties()
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        props.load(localFile.inputStream())
        props.getProperty(propName)
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
    }
    // Fallback to uppercase ENV var (e.g. GEMINI_API_KEY)
    return System.getenv(propName.uppercase(Locale.ROOT)) ?: ""
}

/** Retrieves the Gemini API key from local.properties or ENV. */
fun getGeminiApiKey(): String = findLocalProperty("gemini.api.key")

/** Retrieves the Firebase API key from local.properties or ENV. */
fun getFirebaseApiKey(): String = findLocalProperty("firebase.api.key")
