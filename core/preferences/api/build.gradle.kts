plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.android)
}

android {
    namespace = "com.immflyretail.inseat.sampleapp.core.preferences.api"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.coreKtx)

    // DI
    implementation(libs.hilt)
}