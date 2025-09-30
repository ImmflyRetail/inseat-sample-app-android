package com.immflyretail.inseat.sampleapp.checkout.presentation

sealed interface CheckoutScreenEvent {
    data class OnSeatNumberEntered(val seatNumber: String) : CheckoutScreenEvent
    data class OnPromotionEntered(val promotionId: String) : CheckoutScreenEvent
    data class OnApplyForcePromoClicked(val promotionId: String) : CheckoutScreenEvent
    data object OnMakeOrderClicked : CheckoutScreenEvent
    data object OnDetailsClicked : CheckoutScreenEvent
    data object OnClickOrderStatus : CheckoutScreenEvent
    data object OnBackClicked : CheckoutScreenEvent
    data object OnClickKeepShopping : CheckoutScreenEvent
}