package com.immflyretail.inseat.sampleapp.shop.presentation

import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Menu
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopStatus
import com.immflyretail.inseat.sampleapp.shop.presentation.model.TabItem

sealed interface ShopScreenState {
    data object Loading : ShopScreenState
    data class SelectMenu(val menus: List<Menu>) : ShopScreenState
    data class Error(val message: String?) : ShopScreenState
    data class DataLoaded(
        val shopStatus: ShopStatus,
        val items: List<ShopItem>,
        val isPullToRefreshEnabled: Boolean,
        val isRefreshing: Boolean = false,
        val selectedTabIndex: Int = 0,
        val tabs: List<TabItem> = emptyList(),
        val itemsInBasket: Int = 0,
        val ordersCount: Int,
        val isSearchEnabled: Boolean = false,
        val searchQuery: String = "",
        val searchResult: List<ShopItem> = emptyList(),
    ) : ShopScreenState
}