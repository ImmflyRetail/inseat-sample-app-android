package com.immflyretail.inseat.sampleapp.shop.presentation.model

import com.immflyretail.inseat.sdk.api.models.StatusEnum

enum class ShopStatus {
    OPEN,
    ORDER,
    CLOSED,
    DEFAULT
}

fun StatusEnum.toShopStatus(): ShopStatus {
    return when (this) {
        StatusEnum.OPEN -> ShopStatus.OPEN
        StatusEnum.ORDER -> ShopStatus.ORDER
        StatusEnum.CLOSED -> ShopStatus.CLOSED
    }
}