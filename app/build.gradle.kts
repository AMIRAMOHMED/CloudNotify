import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
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


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    val room_version = "2.6.1"

    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)

    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)
    kapt ("androidx.room:room-compiler:2.5.0")
    // Retrofit and Gson dependencies
    implementation(libs.squareup.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    dependencies {
        val lifecycle_version = "2.8.6"

        // ViewModel
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
        // ViewModel utilities for Compose
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
        // LiveData
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
}
