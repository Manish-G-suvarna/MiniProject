plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    kotlin("plugin.serialization") version "1.8.0"
}

android {
    namespace = "com.example.miniproject"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.miniproject"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // keep this — it requires buildFeatures.buildConfig = true below
        buildConfigField(
            "String",
            "WEATHER_API_KEY",
            "\"${project.properties["WEATHER_API_KEY"] ?: ""}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // <-- ENABLE BUILD CONFIG HERE AND KEEP COMPOSE
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // If using compose BOM, keep compose options as before (optional)
}


dependencies {

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.appcompat)

    // Coil Image Loader
    implementation("io.coil-kt:coil-compose:2.5.0")

    // --- FIREBASE (Use ONE BOM only!) ---
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Firebase modules (no versions needed when using BOM)
    implementation ("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Material Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Retrofit + Serialization
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // OkHttp
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    // Google Location + Places
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Gson (if needed)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.compose.foundation)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
