import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

// Load properties from secrets.properties file
val localProperties = File(rootProject.rootDir, "secrets.properties")
val properties = Properties()

if (localProperties.exists()) {
    properties.load(localProperties.inputStream())
}

android {
    namespace = "com.example.cloudnotify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cloudnotify"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_KEY", "\"${properties["API_KEY"]}\"")
        }
        release {
            buildConfigField("String", "API_KEY", "\"${properties["API_KEY"]}\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kapt {
        correctErrorTypes = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Google Play Services
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    // AndroidX Libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.material)

    // Room Components
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Lottie
    implementation("com.airbnb.android:lottie:6.0.0")

    // Retrofit and Gson
    implementation(libs.squareup.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Lifecycle Components
    val lifecycle_version = "2.8.6"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")

    // Map Library
    implementation("org.osmdroid:osmdroid-android:6.1.12")

    // Dagger Hilt
    implementation ("com.google.dagger:hilt-android:2.48")
    kapt ("com.google.dagger:hilt-compiler:2.48")


    // Testing Libraries
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0-RC.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
}
