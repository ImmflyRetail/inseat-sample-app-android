package com.immflyretail.inseat.sampleapp.orders.presentation.status

import androidx.navigation.NavController

sealed interface OrderStatusScreenActions {
    data class Navigate(val lambda: NavController.()-> Unit) : OrderStatusScreenActions
    data object ShowBottomSheet : OrderStatusScreenActions
}