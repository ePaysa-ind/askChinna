import org.gradle.api.JavaVersion
import java.util.Properties

// Standard app configuration section
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)

}

android {
    namespace = "com.example.askchinna"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.askchinna"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add metadata for privacy policy URL
        manifestPlaceholders["privacyPolicyUrl"] = "https://github.com/ePaysa-ind/askChinna/blob/master/PRIVACY_POLICY.md"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Access the API key from local.properties for release
            buildConfigField("String", "GEMINI_API_KEY", "\"${getApiKey()}\"")
        }

        debug {
            // For debug builds, also use the same API key
            buildConfigField("String", "GEMINI_API_KEY", "\"${getApiKey()}\"")
        }
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
        viewBinding = true
        buildConfig = true  // Enable BuildConfig generation
    }
}

dependencies {
    // Existing dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.android) // Use alias
    kapt(libs.hilt.compiler)         // Use alias


    implementation("com.google.android.material:material:1.12.0") // parent theme for material 3, stable version for Values folder

    //firebase and firestore dependencies required for android mobile auth

    implementation(platform("com.google.firebase:firebase-bom:33.12.0")) //don't change the version
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")

    // --- Add Room Dependencies ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Optional but recommended
    kapt(libs.androidx.room.compiler)     // Crucial for Kapt to process Room annotations
    // --- End Room Dependencies ---

    // Google Gemini API
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Added privacy-related dependencies
    // For secure storage of any sensitive user data
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    //splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // For location services (if doing India-only geo-verification)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // For WebView to display privacy policy
    implementation("androidx.webkit:webkit:1.8.0")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Add this function at the end of the file, outside of the android block
fun getApiKey(): String {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
        return properties.getProperty("gemini.api.key", "")
    }

    return System.getenv("GEMINI_API_KEY") ?: ""
}