package com.immflyretail.inseat.sampleapp.basket.presentation.checkout

import androidx.navigation.NavController

sealed interface CheckoutScreenActions {
    data class Navigate(val lambda: NavController.() -> Unit) : CheckoutScreenActions
    data class ShowDialog(val isOrderSuccess: Boolean) : CheckoutScreenActions
}