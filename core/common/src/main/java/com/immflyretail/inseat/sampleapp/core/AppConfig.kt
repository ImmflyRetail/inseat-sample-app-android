package com.immflyretail.inseat.sampleapp.core

data class AppConfig(
    val isDebug: Boolean,
    val appVersion: String,
    val sdkVersion: String,
    val environment: AppEnvironment,
    val supportedICAOs: List<String>,
)

enum class AppEnvironment {
    TEST, LIVE
}