package com.immflyretail.inseat.sampleapp.shop.presentation

sealed interface ShopScreenActions {
    data class Message(val text: String) : ShopScreenActions
}