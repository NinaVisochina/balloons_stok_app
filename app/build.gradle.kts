plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ua.kulky.stok"
    compileSdk = 35

    defaultConfig {
        applicationId = "ua.kulky.stok"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }

    buildFeatures { compose = true }

    // Java 17 + desugaring
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    // (необов’язково, але ок) дублюємо для Kotlin
    kotlinOptions { jvmTarget = "17" }

    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

// Рекомендовано для Kotlin 2.x
kotlin { jvmToolchain(17) }

dependencies {
    // Compose BOM — керує версіями всіх Compose-артефактів
    val composeBom = platform("androidx.compose:compose-bom:2024.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3") // без версії — бере з BOM
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Room (локальна БД)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Lifecycle / ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")

    // Material (ресурсні стилі для теми)
    implementation("com.google.android.material:material:1.12.0")

    // Desugaring для Java 8+ API на старих SDK
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
