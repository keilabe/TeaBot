// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) version "8.5.1" apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:8.5.1")
        classpath ("com.google.gms:google-services:4.4.2")
    }
}
allprojects{
    repositories {
        google()
        mavenCentral()
    }
}

