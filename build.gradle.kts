// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
}
dependencies {
    libs.hilt
    libs.hiltCompiler
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        libs.hilt
        libs.hiltCompiler
    }
}
