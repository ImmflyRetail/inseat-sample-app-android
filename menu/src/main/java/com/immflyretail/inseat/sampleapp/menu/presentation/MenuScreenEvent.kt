package com.immflyretail.inseat.sampleapp.menu.presentation

import com.immflyretail.inseat.sdk.api.models.Menu

sealed interface MenuScreenEvent {
    data class OnMenuSelected(val menu: Menu) : MenuScreenEvent
    data object OnRefresh : MenuScreenEvent
    data object OnSettingsClicked : MenuScreenEvent
    data object OnBackClicked : MenuScreenEvent
}