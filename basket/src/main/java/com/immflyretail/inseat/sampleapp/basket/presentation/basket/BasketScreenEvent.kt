package com.immflyretail.inseat.sampleapp.basket.presentation.basket

sealed interface BasketScreenEvent {
    data class OnRemoveItemClicked(val itemId: Int) : BasketScreenEvent
    data class OnAddItemClicked(val itemId: Int) : BasketScreenEvent
}