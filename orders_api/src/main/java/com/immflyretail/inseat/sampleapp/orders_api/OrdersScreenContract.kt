package com.immflyretail.inseat.sampleapp.orders_api

import kotlinx.serialization.Serializable

object OrdersScreenContract {

    @Serializable
    data object OrdersListRoute

    @Serializable
    data class OrderStatusRoute(val orderId: String)
}