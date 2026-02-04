plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.immflyretail.inseat.sampleapp.promotion"

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    // Core
    implementation(project(Modules.core_common))
    implementation(project(Modules.core_navigation))
    implementation(project(Modules.core_ui))
    implementation(project(Modules.core_extension))
    implementation(project(Modules.core_preferences_api))
    implementation(project(Modules.core_resources))

    api(project(Modules.promotion_api))
    implementation(project(Modules.basket_api))

    implementation(libs.inseat)
    // Libs
    implementation(libs.coreKtx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.fragmentKtx)
    implementation(libs.gson)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // DI
    implementation(libs.hilt)
    ksp(libs.hilt.android.compiler)
}