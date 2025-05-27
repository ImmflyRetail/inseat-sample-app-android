package com.immflyretail.inseat.sampleapp.shop.presentation

import com.immflyretail.inseat.sdk.api.models.Menu

sealed interface ShopScreenEvent {
    data class OnAddItemClicked(val itemId: Int) : ShopScreenEvent
    data class OnRemoveItemClicked(val itemId: Int) : ShopScreenEvent
    data class OnMenuSelected(val menu: Menu) : ShopScreenEvent
    data object OnRefresh : ShopScreenEvent
    data object ClickOnCategories : ShopScreenEvent
    data object CloseCategories : ShopScreenEvent
}