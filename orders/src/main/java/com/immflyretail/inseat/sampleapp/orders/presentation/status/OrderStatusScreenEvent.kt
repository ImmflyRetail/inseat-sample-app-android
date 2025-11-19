package com.immflyretail.inseat.sampleapp.orders.presentation.status

sealed interface OrderStatusScreenEvent  {
    data object OnCancelOrderClicked : OrderStatusScreenEvent
    data object OnBackClicked : OrderStatusScreenEvent
}
