package com.immflyretail.inseat.sampleapp.orders.presentation.status

import com.immflyretail.inseat.sdk.api.models.Order

sealed interface OrderStatusScreenState {
    data class Data(val order: Order) : OrderStatusScreenState
    data class Error(val message: String) : OrderStatusScreenState
    data object Loading : OrderStatusScreenState
}