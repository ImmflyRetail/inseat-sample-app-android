plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.immflyretail.inseat.sampleapp.core.extension"

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(libs.coreKtx)
    implementation(libs.fragmentKtx)
    implementation(libs.navigation)
    implementation(libs.bundles.compose)
}