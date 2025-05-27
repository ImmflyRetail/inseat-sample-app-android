package com.immflyretail.inseat.sampleapp.basket.presentation.checkout

import com.immflyretail.inseat.sampleapp.basket.presentation.basket.model.BasketItem
import java.math.BigDecimal

sealed interface CheckoutScreenState {
    data class Data(
        val items: List<BasketItem>,
        val total: BigDecimal,
        val currency: String,
        val seatNumber: String = "",
        val isExpanded: Boolean = false,
    ) : CheckoutScreenState

    data class Error(val message: String) : CheckoutScreenState
    data object Loading : CheckoutScreenState
}