package com.immflyretail.inseat.sampleapp.product_api

import kotlinx.serialization.Serializable

object ProductScreenContract {

    @Serializable
    data class Route(val id: Int)
}