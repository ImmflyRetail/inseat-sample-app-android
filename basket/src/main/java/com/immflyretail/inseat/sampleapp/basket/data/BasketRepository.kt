package com.immflyretail.inseat.sampleapp.basket.data

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.BASKET
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.Product
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface BasketRepository {
    suspend fun fetchProductList(queryIds: List<Int>):List<Product>
    suspend fun setBasketItemsJSON(json: String)
    suspend fun getBasketItemsJSON(): String
}

internal class BasketRepositoryImpl @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val inseatApi: InseatApi,
    private val prefManager: PreferencesManager
) : BasketRepository {

    override suspend fun fetchProductList(queryIds: List<Int>) = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchProducts(queryIds)
    }

    override suspend fun getBasketItemsJSON(): String {
        return prefManager.read(BASKET, "")
    }

   override suspend fun setBasketItemsJSON(json: String) {
         prefManager.write(BASKET, json)
    }
}