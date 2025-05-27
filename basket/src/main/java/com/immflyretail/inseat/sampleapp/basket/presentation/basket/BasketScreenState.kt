package com.immflyretail.inseat.sampleapp.basket.presentation.basket

import com.immflyretail.inseat.sampleapp.basket.presentation.basket.model.BasketItem
import java.math.BigDecimal

sealed interface BasketScreenState {
    data object Loading : BasketScreenState
    data class DataLoaded(
        val items: List<BasketItem>,
        val total: BigDecimal,
        val currency: String,
    ) : BasketScreenState

    data class Error(val message: String) : BasketScreenState
}