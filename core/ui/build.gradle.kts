plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.immflyretail.inseat.sampleapp.ui"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.bundles.compose)

    implementation(libs.coreKtx)
    implementation(libs.appcompat)
    implementation(libs.material)
}