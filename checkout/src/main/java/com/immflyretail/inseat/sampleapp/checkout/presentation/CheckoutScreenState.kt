package com.immflyretail.inseat.sampleapp.checkout.presentation

import com.immflyretail.inseat.sampleapp.checkout.presentation.models.BasketItem
import java.math.BigDecimal

sealed interface CheckoutScreenState {
    data class Data(
        val items: List<BasketItem>,
        val total: BigDecimal,
        val currency: String,
        val seatNumber: String = "",
        val isExpanded: Boolean = false,
        val savings: BigDecimal,
        val enteredPromotionId: String = ""
    ) : CheckoutScreenState

    data class Error(val message: String) : CheckoutScreenState
    data object Loading : CheckoutScreenState
}