package com.immflyretail.inseat.sampleapp.checkout.presentation.models

import com.immflyretail.inseat.sdk.api.models.Product

data class BasketItem(val quantity: Int, val product: Product)