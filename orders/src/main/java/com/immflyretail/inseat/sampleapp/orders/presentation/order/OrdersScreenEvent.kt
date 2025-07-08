package com.immflyretail.inseat.sampleapp.orders.presentation.order

sealed interface OrdersScreenEvent {
    data class OnCancelOrderClicked(val orderId: String) : OrdersScreenEvent
    data class OnDetailsClicked(val orderId: String) : OrdersScreenEvent
    data object OnBackClicked : OrdersScreenEvent
}