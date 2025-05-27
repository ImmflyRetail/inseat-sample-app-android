plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.android)
}

android {
    namespace = "com.immflyretail.inseat.sampleapp.core.common"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.coreKtx)
}