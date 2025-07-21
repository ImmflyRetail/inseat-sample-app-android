package com.immflyretail.inseat.sampleapp.product.data

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.BASKET
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.Product
import com.immflyretail.inseat.sdk.api.models.Shop
import com.immflyretail.inseat.sdk.api.models.ShopInfo
import com.immflyretail.inseat.sdk.api.models.StatusEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

interface ProductRepository {
    suspend fun fetchProduct(id: Int): Product
    suspend fun updateBasketItem(id: Int, amount: Int)
    suspend fun getSelectedAmount(productId: Int): Int
    suspend fun observeShopAvailableStatus(): Flow<Boolean>
}

internal class ProductRepositoryImpl @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val inseatApi: InseatApi,
    private val preferencesManager: PreferencesManager
) : ProductRepository {

    override suspend fun fetchProduct(id: Int): Product = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchProducts(listOf(id)).first()
    }


    override suspend fun updateBasketItem(id: Int, amount: Int) {
        preferencesManager.read(BASKET, "").let { json ->
            val basketItems = if (json.isNotEmpty()) {
                Json.decodeFromString<Map<Int, Int>>(json).toMutableMap()
            } else {
                mutableMapOf()
            }
            when (amount) {
                0 -> basketItems.remove(id)
                else -> basketItems[id] = amount
            }
            val updatedJson = Json.encodeToString(basketItems)
            preferencesManager.write(BASKET, updatedJson)
        }
    }

    override suspend fun getSelectedAmount(productId: Int): Int {
        preferencesManager.read(BASKET, "").let { json ->
            return if (json.isNotEmpty()) {
                Json.decodeFromString<Map<Int, Int>>(json)[productId] ?: 0
            } else {
                0
            }
        }
    }

    override suspend fun observeShopAvailableStatus(): Flow<Boolean> = withContext(dispatchersProvider.getIO()) {
        inseatApi.observeShop().map { shopInfo ->
            shopInfo is Shop && shopInfo.status == StatusEnum.ORDER
        }
    }
}