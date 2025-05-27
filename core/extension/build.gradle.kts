plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.android)
}

android {
    namespace = "com.immflyretail.inseat.sampleapp.core.extension"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.coreKtx)
    implementation(libs.fragmentKtx)
}