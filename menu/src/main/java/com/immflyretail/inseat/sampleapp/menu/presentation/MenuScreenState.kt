package com.immflyretail.inseat.sampleapp.menu.presentation

import com.immflyretail.inseat.sdk.api.models.Menu

sealed interface MenuScreenState {
    data object Loading : MenuScreenState
    data class Error(val message: String?) : MenuScreenState
    data class DataLoaded(
        val menus: List<Menu>,
        val isPullToRefreshEnabled: Boolean,
        val isRefreshing: Boolean = false,
    ) : MenuScreenState
}