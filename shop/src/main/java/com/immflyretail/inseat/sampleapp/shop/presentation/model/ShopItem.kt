package com.immflyretail.inseat.sampleapp.shop.presentation.model

import com.immflyretail.inseat.sdk.api.models.Product

data class ShopItem(val product: Product, val selectedQuantity: Int= 0)