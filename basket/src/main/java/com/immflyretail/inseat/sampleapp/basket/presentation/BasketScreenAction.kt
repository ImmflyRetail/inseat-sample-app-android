package com.immflyretail.inseat.sampleapp.basket.presentation

import androidx.navigation.NavController

sealed interface BasketScreenActions {
    data class Navigate(val lambda: NavController.()-> Unit) : BasketScreenActions
}