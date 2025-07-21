package com.immflyretail.inseat.sampleapp.basket.presentation.basket

sealed interface BasketScreenEvent {
    data object OnBackClicked : BasketScreenEvent
    data object OnAddMoreClicked : BasketScreenEvent
    data object OnCheckoutClicked : BasketScreenEvent
    data class OnRemoveItemClicked(val itemId: Int) : BasketScreenEvent
    data class OnAddItemClicked(val itemId: Int) : BasketScreenEvent
    data class OnItemClicked(val itemId: Int) : BasketScreenEvent
}