import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

//  Load from local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

val MAP_API_KEY = localProperties["MAP_KEY_API"] as String? ?: ""

android {
    namespace = "com.example.workout"
    compileSdk = 35

    signingConfigs {
        /*Alters the current debug config
        This way anyone in the team that wants to debug
        have access to the Database without adding their SHA-1 each time
        for each platform they use*/
        named("debug") {
            storeFile = file("keystores/team-debug.keystore")
            storePassword = "workout123"
            keyAlias = "teamDebugKey"
            keyPassword = "workout123"
        }
    }

    defaultConfig {
        applicationId = "com.example.workout"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "google_maps_key", MAP_API_KEY)
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // To enable MVVM architecture using DataBinding
    buildFeatures {
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.room.common.jvm)
    implementation(libs.firebase.database)
    implementation(libs.googleid)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("org.mockito:mockito-android:5.+")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Core Credential Manager:
    implementation("androidx.credentials:credentials:1.0.1")
    // Google ID helper library for parsing/validating tokens
    implementation("com.google.android.libraries.identity.googleid:googleid:<latest-version>")
    // Google Maps SDK for Android
    implementation("com.google.android.gms:play-services-maps:19.2.0")

    // Fused Location Provider (high-accuracy, battery-optimized)
    implementation("com.google.android.gms:play-services-location:21.3.0")

}