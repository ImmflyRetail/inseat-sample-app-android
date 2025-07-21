import Modules.inseat
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose.compiler)
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

android {
    namespace = "com.immflyretail.inseat.sampleapp"

    defaultConfig {
        applicationId = "com.immflyretail.inseat.sampleapp"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        buildConfigField("String", "API_KEY", "\"${localProps.getProperty("API_KEY", "")}\"")
    }

    buildTypes {
        getByName(AppEnvironment.RELEASE.value) {
            isMinifyEnabled = true
            initWith(getByName(AppEnvironment.DEBUG.value))
            matchingFallbacks += AppEnvironment.DEBUG.value
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName(AppEnvironment.DEBUG.value)
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {

    // Libs
    implementation(libs.coreKtx)
    implementation(libs.appcompat)
    implementation(libs.timber)
    implementation(libs.navigation)
    implementation(libs.kotlinx.serialization)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.ui.text.google.fonts)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Modules
    implementation(project(Modules.core_navigation))
    implementation(project(Modules.core_ui))
    implementation(project(Modules.basket))
    implementation(project(Modules.shop))
    implementation(project(Modules.core_preferences_impl))
    implementation(project(Modules.settings))
    implementation(project(Modules.orders))
    implementation(project(Modules.product))
    implementation(project(Modules.checkout))

    // Inseat SDK
    implementation(libs.inseat)

    // DI
    implementation(libs.hilt)
    ksp(libs.hilt.android.compiler)
}
