

plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.immflyretail.inseat.sampleapp.core.preferences.impl"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Core
    implementation(project(Modules.core_common))
    api(project(Modules.core_preferences_api))

    // Libs
    implementation(libs.coreKtx)
    implementation(libs.timber)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)
    implementation(libs.androidx.security.crypto)

    // DI
    implementation(libs.hilt)
    ksp(libs.hilt.android.compiler)
}