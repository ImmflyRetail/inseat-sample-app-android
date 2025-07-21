package com.immflyretail.inseat.sampleapp.checkout.presentation

import androidx.navigation.NavController

sealed interface CheckoutScreenActions {
    data class Navigate(val lambda: NavController.() -> Unit) : CheckoutScreenActions
    data class ShowDialog(val isOrderSuccess: Boolean) : CheckoutScreenActions
}