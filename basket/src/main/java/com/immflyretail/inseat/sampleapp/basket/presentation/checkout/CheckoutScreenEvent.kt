package com.immflyretail.inseat.sampleapp.basket.presentation.checkout

sealed interface CheckoutScreenEvent {
    data class OnSeatNumberEntered(val seatNumber: String) : CheckoutScreenEvent
    data class OnMakeOrderClicked(val navigation: ()-> Unit) : CheckoutScreenEvent
    data object OnDetailsClicked : CheckoutScreenEvent
}