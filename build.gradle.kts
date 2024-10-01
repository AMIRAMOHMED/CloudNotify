// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false

}

buildscript {
repositories {
google()
mavenCentral()
}
dependencies {
classpath("com.android.tools.build:gradle:8.0.0")
classpath("com.google.gms:google-services:4.3.15") // For Google Maps and Firebase
classpath ("com.google.dagger:hilt-android-gradle-plugin:2.48") // For Dagger Hilt
}
}