package com.immflyretail.inseat.sampleapp.promotion.presentation.model

import java.math.BigDecimal

sealed class PromotionBlock(open val promotionItems: List<PromotionItem>) {
     data class ProductPurchaseBlock(
        val expectedSelectedItems: Int,
        var selectedItems: Int = 0,
        override val promotionItems: List<PromotionItem>
    ) : PromotionBlock(promotionItems)

    data class SpendLimitBlock(
        var selectedItemsPrice: BigDecimal,
        override val promotionItems: List<PromotionItem>
    ) : PromotionBlock(promotionItems)
}