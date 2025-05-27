package com.immflyretail.inseat.sampleapp.settings.presentation

sealed interface SettingsScreenState {
    data object Loading : SettingsScreenState
    data class DataLoaded(val isAutoRefreshEnabled: Boolean) : SettingsScreenState
}