package com.immflyretail.inseat.sampleapp.shop.presentation.model

import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Product
import com.immflyretail.inseat.sdk.api.models.Promotion

sealed interface TabItem {
    fun getTabName(): String
    fun getTabId(): Int

    data class CategoryTab(val category: Category, val products: List<Product>) : TabItem {
        override fun getTabName() = category.name
        override fun getTabId() = category.id

    }

    data class PromotionTab(val promotions: List<Promotion>) : TabItem {
        override fun getTabName() = "Promotions"
        override fun getTabId() = -100
    }
}