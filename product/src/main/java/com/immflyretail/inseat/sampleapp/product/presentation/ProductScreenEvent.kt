package com.immflyretail.inseat.sampleapp.product.presentation

sealed interface ProductScreenEvent {
    data object OnAddItemClicked : ProductScreenEvent
    data object OnRemoveItemClicked : ProductScreenEvent
    data object OnBackClicked : ProductScreenEvent
    data object OnConfirmClicked : ProductScreenEvent
}