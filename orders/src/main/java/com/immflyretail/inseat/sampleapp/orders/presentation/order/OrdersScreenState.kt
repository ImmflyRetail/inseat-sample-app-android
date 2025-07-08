package com.immflyretail.inseat.sampleapp.orders.presentation.order

import com.immflyretail.inseat.sdk.api.models.Order

sealed interface OrdersScreenState {
    data class Data(val items: List<Order>, ) : OrdersScreenState

    data class Error(val message: String) : OrdersScreenState
    data object Loading : OrdersScreenState
}