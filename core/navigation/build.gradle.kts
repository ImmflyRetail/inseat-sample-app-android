plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.immflyretail.inseat.sampleapp.navigation"

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(libs.kotlinx.serialization)
    api(libs.androidx.navigation.compose)
    api(libs.androidx.compose.livedata)
}