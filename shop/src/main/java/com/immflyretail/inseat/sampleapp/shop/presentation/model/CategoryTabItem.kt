package com.immflyretail.inseat.sampleapp.shop.presentation.model

import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Product
import com.immflyretail.inseat.sdk.api.models.Promotion
import com.immflyretail.inseat.sdk.api.models.Subcategory

data class CategoryTabItem(
    val category: Category, val products: List<Product>
) {
    fun getTabName() = category.name
    fun getTabId() = category.id
}


sealed interface SubcategoryTabItem {
    fun getTabName(): String
    fun getTabId(): Int

    data class SubcategoryTab(val category: Subcategory, val products: List<Product>) :
        SubcategoryTabItem {
        override fun getTabName() = category.name
        override fun getTabId() = category.id
    }

    data class PromotionTab(val promotions: List<Promotion>) : SubcategoryTabItem {
        override fun getTabName() = "Promotions"
        override fun getTabId() = -100
    }
}