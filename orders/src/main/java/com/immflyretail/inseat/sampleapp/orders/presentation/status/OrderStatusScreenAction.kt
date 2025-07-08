package com.immflyretail.inseat.sampleapp.orders.presentation.status

import androidx.navigation.NavController

sealed interface OrderStatusScreenAction {
    data class Navigate(val lambda: NavController.()-> Unit) : OrderStatusScreenAction
}