package com.immflyretail.inseat.sampleapp.basket.presentation

import com.immflyretail.inseat.sampleapp.basket.presentation.model.BasketItem
import com.immflyretail.inseat.sdk.api.models.AppliedPromotion
import com.immflyretail.inseat.sdk.api.models.Money

sealed interface BasketScreenState {
    data object Loading : BasketScreenState
    data class DataLoaded(
        val items: List<BasketItem>,
        val subtotal: Money,
        val appliedPromotions: List<AppliedPromotion>,
    ) : BasketScreenState

    data class Error(val message: String) : BasketScreenState
}