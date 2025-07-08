package com.immflyretail.inseat.sampleapp.orders.presentation.order

import androidx.navigation.NavController

sealed interface OrderScreenAction {
    data class Navigate(val lambda: NavController.() -> Unit) : OrderScreenAction
}