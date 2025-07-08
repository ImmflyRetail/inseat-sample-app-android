package com.immflyretail.inseat.sampleapp.settings.presentation

import androidx.navigation.NavController

sealed interface SettingsScreenAction {
    data class Navigate(val lambda: NavController.() -> Unit) : SettingsScreenAction
}