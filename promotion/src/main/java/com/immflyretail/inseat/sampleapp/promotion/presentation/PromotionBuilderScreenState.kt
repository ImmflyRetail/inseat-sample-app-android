package com.immflyretail.inseat.sampleapp.promotion.presentation

import com.immflyretail.inseat.sampleapp.promotion.presentation.model.PromotionBlock
import com.immflyretail.inseat.sdk.api.models.Money

sealed interface PromotionBuilderScreenState {
    data object Loading : PromotionBuilderScreenState
    data class Error(val message: String?) : PromotionBuilderScreenState
    data class DataLoaded(
        val isShopAvailable: Boolean,
        val isCompleted: Boolean = false,
        val currency: String,
        val title: String,
        val savings: String,
        val description: String,
        val triggerType: PromotionTriggerType,
        val blocks: List<PromotionBlock>,
    ) : PromotionBuilderScreenState
}

sealed interface PromotionTriggerType {
    data object ProductPurchase : PromotionTriggerType

    data class SpendLimit(
        val haveToSpend: Money,
        var spent: Money,
    ) : PromotionTriggerType
}
