package com.immflyretail.inseat.sampleapp.orders.presentation

sealed interface OrdersScreenEvent {
    data class OnCancelOrderClicked(val orderId: String) : OrdersScreenEvent
}