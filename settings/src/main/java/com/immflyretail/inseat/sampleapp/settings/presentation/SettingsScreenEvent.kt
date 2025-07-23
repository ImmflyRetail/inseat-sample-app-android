package com.immflyretail.inseat.sampleapp.settings.presentation

sealed interface SettingsScreenEvent {
    data object OnAutoRefreshEnabled : SettingsScreenEvent
    data object OnManualRefreshEnabled : SettingsScreenEvent
}