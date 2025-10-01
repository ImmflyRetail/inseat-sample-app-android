package com.immflyretail.inseat.sampleapp.promotion_api

import kotlinx.serialization.Serializable

object PromotionContract {
    @Serializable
    data class Route(val promotionId: Int)
}