package com.immflyretail.inseat.sampleapp.shop.presentation

import com.immflyretail.inseat.sampleapp.shop.presentation.model.CategoryTabItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.SubcategoryTabItem

sealed interface ShopScreenEvent {
    data class OnProductClicked(val itemId: Int) : ShopScreenEvent
    data class OnPromotionClicked(val promotionId: Int) : ShopScreenEvent
    data class OnAddItemClicked(val itemId: Int) : ShopScreenEvent
    data class OnRemoveItemClicked(val itemId: Int) : ShopScreenEvent
    data class OnTabSelected(val tab: CategoryTabItem, val selectedCategoryIndex: Int) : ShopScreenEvent
    data class OnSubTabSelected(val subTab: SubcategoryTabItem, val selectedSubcategoryIndex: Int) : ShopScreenEvent
    data object OnRefresh : ShopScreenEvent
    data object OnSettingsClicked : ShopScreenEvent
    data object OnBackClicked : ShopScreenEvent
    data object OnCartClicked : ShopScreenEvent
    data object OnOrdersClicked : ShopScreenEvent
    data object OnSearchClicked : ShopScreenEvent
    data class OnSearch(val query: String) : ShopScreenEvent
}