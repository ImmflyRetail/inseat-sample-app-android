package com.immflyretail.inseat.sampleapp.orders.presentation.status

sealed interface OrderStatusScreenEvent  {
    data class OnCancelOrderClicked(val orderId: String) : OrderStatusScreenEvent
    data object OnBackClicked : OrderStatusScreenEvent
}
