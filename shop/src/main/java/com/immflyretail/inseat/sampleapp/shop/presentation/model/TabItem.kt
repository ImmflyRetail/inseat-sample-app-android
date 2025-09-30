package com.immflyretail.inseat.sampleapp.shop.presentation.model

import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Promotion

sealed interface TabItem {
    fun getTabName(): String
    data class CategoryTab(val category: Category) : TabItem {
        override fun getTabName() = category.name

    }

    data class PromotionTab(val promotions: List<Promotion>) : TabItem {
        override fun getTabName() = "Promotions"
    }
}