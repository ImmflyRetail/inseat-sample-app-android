package com.immflyretail.inseat.sampleapp.shop.presentation

import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Menu
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopStatus

sealed interface ShopScreenState {
    data object Loading : ShopScreenState
    data class SelectMenu(val menus: List<Menu>) : ShopScreenState
    data class Error(val message: String?) : ShopScreenState
    data class DataLoaded(
        val shopStatus: ShopStatus,
        val items: List<ShopItem>,
        val isPullToRefreshEnabled: Boolean,
        val isRefreshing: Boolean = false,
        val categories: List<Category>? = null,
        val itemsInBasket: Int = 0,
    ) : ShopScreenState
}