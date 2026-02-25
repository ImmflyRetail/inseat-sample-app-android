package com.immflyretail.inseat.sampleapp.menu.presentation

import androidx.navigation.NavController

sealed interface MenuScreenActions {
    data class Navigate(val lambda: NavController.() -> Unit) : MenuScreenActions
}