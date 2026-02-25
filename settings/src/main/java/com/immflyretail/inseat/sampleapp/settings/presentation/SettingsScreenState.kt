package com.immflyretail.inseat.sampleapp.settings.presentation

sealed interface SettingsScreenState {
    data object Loading : SettingsScreenState
    data class DataLoaded(
        val isDebug: Boolean,
        val isAutoRefreshEnabled: Boolean,
        val appVersion: String,
        val sdkVersion: String,
        val environment: String,
        val supportedICAOs: String,
    ) : SettingsScreenState
}