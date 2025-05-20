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
        // create a new config named "debug"
        named("debug") {
            storeFile = file("keystores/team-debug.keystore")
            storePassword = "workout123"
            keyAlias = "teamDebugKey"
            keyPassword = "workout123"
        }
    }

    defaultConfig {
        applicationId = "com.example.workout"
        minSdk = 24
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    // Retrofit core - for APIServices
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Retrofit → Gson converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Gson library
    implementation("com.google.code.gson:gson:2.8.9")

    // Core Credential Manager:
    implementation("androidx.credentials:credentials:<latest-version>")

    // The “play-services-auth” bridge that brings in the Google federated providers
    implementation("androidx.credentials:credentials-play-services-auth:<latest-version>")

    // Google ID helper library for parsing/validating tokens
    implementation("com.google.android.libraries.identity.googleid:googleid:<latest-version>")
    implementation("androidx.credentials:credentials:1.0.1")

}