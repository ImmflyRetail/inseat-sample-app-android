package com.immflyretail.inseat.sampleapp.shop.data

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.AUTO_REFRESH
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.BASKET
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Menu
import com.immflyretail.inseat.sdk.api.models.Product
import com.immflyretail.inseat.sdk.api.models.Promotion
import com.immflyretail.inseat.sdk.api.models.ShopInfo
import com.immflyretail.inseat.sdk.api.models.UserData
import com.immflyretail.inseat.sdk.impl.c
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

interface ShopRepository {
    suspend fun getProductsObserver(category: Category): StateFlow<List<Product>>
    suspend fun getShopObserver(): StateFlow<ShopInfo>
    suspend fun fetchShop(): ShopInfo
    suspend fun fetchProducts(category: Category): List<Product>
    suspend fun fetchCategories(): List<Category>
    suspend fun fetchPromotions(): List<Promotion>
    suspend fun fetchOrderCount(): Flow<Int>
    fun getBasketItemsFlow(): Flow<Map<Int, Int>>
    suspend fun removeFromBasketItem(id: Int)
    suspend fun addToBasketItem(id: Int)
    suspend fun setSelectionToBasketItem(id: Int, selected: Int)
    suspend fun selectMenu(menu: Menu)
    suspend fun isMenuSelected(): Boolean
    suspend fun getAvailableMenus(): List<Menu>
    suspend fun isAutoupdateEnabled(): Boolean
}

internal class ListRepositoryImpl @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val inseatApi: InseatApi,
    private val preferencesManager: PreferencesManager
) : ShopRepository {

    override suspend fun getProductsObserver(category: Category) = withContext(dispatchersProvider.getIO()) {
        inseatApi.observeProducts(category)
    }

    override suspend fun getShopObserver() = withContext(dispatchersProvider.getIO()) {
        inseatApi.observeShop()
    }

    override suspend fun fetchShop() = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchShop()
    }

    override suspend fun fetchProducts(category: Category): List<Product> = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchProducts(category = category)
    }

    override suspend fun fetchPromotions(): List<Promotion> = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchPromotions()
    }

    override suspend fun isAutoupdateEnabled(): Boolean {
        return preferencesManager.read(AUTO_REFRESH, true)
    }

    override fun getBasketItemsFlow(): Flow<Map<Int, Int>> = preferencesManager.asFlow(BASKET, "{}")
        .map { Json.decodeFromString<Map<Int, Int>>(it) }

    override suspend fun addToBasketItem(id: Int) {
        val basket = Json.decodeFromString<Map<Int, Int>>(preferencesManager.read(BASKET, "{}")).toMutableMap()
        basket[id] = (basket[id] ?: 0) + 1
        preferencesManager.write(BASKET, Json.encodeToString(basket))
    }

    override suspend fun removeFromBasketItem(id: Int) {
        val basket = Json.decodeFromString<Map<Int, Int>>(preferencesManager.read(BASKET, "{}")).toMutableMap()
        val quantity = (basket[id] ?: 0) - 1
        if (quantity > 0)
            basket[id] = quantity
        else {
            basket.remove(id)
        }
        preferencesManager.write(BASKET, Json.encodeToString(basket))
    }

    override suspend fun setSelectionToBasketItem(id: Int, selected: Int) {
        val basket = Json.decodeFromString<Map<Int, Int>>(preferencesManager.read(BASKET, "{}")).toMutableMap()
        basket[id] = selected
        preferencesManager.write(BASKET, Json.encodeToString(basket))
    }

    override suspend fun selectMenu(menu: Menu) {
        inseatApi.setUserData(UserData(menu))
    }

    override suspend fun isMenuSelected(): Boolean {
        return inseatApi.fetchSelectedMenu() != null
    }

    override suspend fun getAvailableMenus(): List<Menu> = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchMenus()
    }

    override suspend fun fetchCategories(): List<Category> = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchCategories()
    }

    override suspend fun fetchOrderCount(): Flow<Int> = withContext(dispatchersProvider.getIO()) {
        inseatApi.observeOrders().map { orders -> orders.size }
    }
}