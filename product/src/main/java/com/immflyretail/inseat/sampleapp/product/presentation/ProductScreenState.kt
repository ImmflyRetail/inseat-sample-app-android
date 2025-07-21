package com.immflyretail.inseat.sampleapp.product.presentation

import com.immflyretail.inseat.sdk.api.models.Product

sealed interface ProductScreenState {
    data class Data(
        val product: Product,
        val selectedAmount: Int,
        val isShopAvailable: Boolean,
    ) : ProductScreenState

    data class Error(val message: String) : ProductScreenState
    data object Loading : ProductScreenState
}