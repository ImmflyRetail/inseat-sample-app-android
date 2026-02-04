package com.immflyretail.inseat.sampleapp.basket.presentation.model

import com.immflyretail.inseat.sdk.api.models.Product

data class BasketItem(val quantity: Int, val product: Product)