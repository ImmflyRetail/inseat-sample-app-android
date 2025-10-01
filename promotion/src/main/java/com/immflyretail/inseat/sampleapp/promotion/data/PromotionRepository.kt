package com.immflyretail.inseat.sampleapp.promotion.data

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.BASKET
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sampleapp.promotion.presentation.model.PromotionItem
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Product
import com.immflyretail.inseat.sdk.api.models.Promotion
import com.immflyretail.inseat.sdk.api.models.PromotionCategory
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

interface PromotionRepository {
    suspend fun fetchProducts(category: Category? = null): List<Product>
    suspend fun fetchPromotion(promotionId: Int): Promotion
    suspend fun fetchPromotionCategories(): List<PromotionCategory>
    suspend fun addToBasket(items: List<PromotionItem>)
}

internal class PromotionRepositoryImpl @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val inseatApi: InseatApi,
    private val preferencesManager: PreferencesManager
) : PromotionRepository {

    override suspend fun fetchProducts(category: Category?): List<Product> = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchProducts(category = category)
    }

    override suspend fun fetchPromotionCategories(): List<PromotionCategory> = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchPromotionCategories()
    }

    override suspend fun fetchPromotion(promotionId: Int): Promotion = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchPromotions().find { it.promotionId == promotionId } ?: throw IllegalArgumentException("Promotion with id $promotionId not found")
    }

    override suspend fun addToBasket(items: List<PromotionItem>) {
        val basket = try {
            Json.decodeFromString<Map<Int, Int>>(preferencesManager.read(BASKET, "")).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
        items.forEach { item ->
            basket[item.product.itemId] = (basket[item.product.itemId] ?: 0) + item.selectedQuantity
        }
        preferencesManager.write(BASKET, Json.encodeToString(basket))
    }
}