package com.immflyretail.inseat.sampleapp.shop.presentation

import androidx.navigation.NavController

sealed interface ShopScreenActions {
    data class Navigate(val lambda: NavController.() -> Unit) : ShopScreenActions
}