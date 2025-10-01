package com.immflyretail.inseat.sampleapp.promotion.presentation

import androidx.navigation.NavController

sealed interface PromotionBuilderScreenActions {
    data class Navigate(val lambda: NavController.() -> Unit) : PromotionBuilderScreenActions
}