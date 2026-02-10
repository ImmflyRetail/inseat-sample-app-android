package com.immflyretail.inseat.sampleapp.product.presentation

import androidx.navigation.NavController

sealed interface ProductScreenActions {
    data class Navigate(val lambda: NavController.() -> Unit) : ProductScreenActions
}