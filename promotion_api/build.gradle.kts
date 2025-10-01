plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.android)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.immflyretail.inseat.sampleapp.promotion_api"
}

dependencies {
    implementation(libs.coreKtx)
    implementation(libs.kotlinx.serialization)
}