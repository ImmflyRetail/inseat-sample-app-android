package com.immflyretail.inseat.sampleapp.product_api

import java.io.Serializable


enum class ProductScreenResultKey {
    REFRESHED_PRODUCT
}

data class ProductScreenResult(val productId: Int =  -1, val selectedAmount: Int = -1): Serializable
