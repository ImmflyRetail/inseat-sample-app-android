package com.immflyretail.inseat.sampleapp.orders.presentation.order

import androidx.navigation.NavController

sealed interface OrdersScreenActions {
    data class Navigate(val lambda: NavController.() -> Unit) : OrdersScreenActions

    data object ShowBottomSheet : OrdersScreenActions
}