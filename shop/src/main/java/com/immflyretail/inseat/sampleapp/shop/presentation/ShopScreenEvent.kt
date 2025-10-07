package com.immflyretail.inseat.sampleapp.shop.presentation

import com.immflyretail.inseat.sampleapp.shop.presentation.model.TabItem
import com.immflyretail.inseat.sdk.api.models.Menu

sealed interface ShopScreenEvent {
    data class OnProductClicked(val itemId: Int) : ShopScreenEvent
    data class OnPromotionClicked(val promotionId: Int) : ShopScreenEvent
    data class OnAddItemClicked(val itemId: Int) : ShopScreenEvent
    data class OnRemoveItemClicked(val itemId: Int) : ShopScreenEvent
    data class OnMenuSelected(val menu: Menu) : ShopScreenEvent
    data class OnTabSelected(val tab: TabItem, val selectedTabIndex: Int) : ShopScreenEvent
    data object OnRefresh : ShopScreenEvent
    data object OnSettingsClicked : ShopScreenEvent
    data object OnBackClicked : ShopScreenEvent
    data object OnCartClicked : ShopScreenEvent
    data object OnOrdersClicked : ShopScreenEvent
    data object OnSearchClicked : ShopScreenEvent
    data class OnSearch(val query: String) : ShopScreenEvent
}