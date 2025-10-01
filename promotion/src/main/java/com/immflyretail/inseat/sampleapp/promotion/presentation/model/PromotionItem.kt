package com.immflyretail.inseat.sampleapp.promotion.presentation.model

import com.immflyretail.inseat.sdk.api.models.Product

data class PromotionItem(val product: Product, var selectedQuantity: Int = 0)