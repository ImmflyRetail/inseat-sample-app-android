package com.immflyretail.inseat.sampleapp.promotion.presentation

sealed interface PromotionBuilderScreenEvent {
    data class OnAddItemClicked(val itemId: Int) : PromotionBuilderScreenEvent
    data class OnRemoveItemClicked(val itemId: Int) : PromotionBuilderScreenEvent
    data object OnBackClicked : PromotionBuilderScreenEvent
    data object AddToCartClicked : PromotionBuilderScreenEvent
    data class OnProductUpdated(val productId: Int, val selectedAmount: Int) : PromotionBuilderScreenEvent
}