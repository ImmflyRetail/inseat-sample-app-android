package com.immflyretail.inseat.sampleapp.checkout.data

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.BASKET
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.Order
import com.immflyretail.inseat.sdk.api.models.Product
import com.immflyretail.inseat.sdk.api.models.Shop
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface CheckoutRepository {
    suspend fun fetchProductList(queryIds: List<Int>):List<Product>
    suspend fun makeOrder(order: Order, callback: (Result<Unit>) -> Unit)
    suspend fun getBasketItemsJSON(): String
    suspend fun getShiftId(): String
    suspend fun clearBasket()
}

internal class CheckoutRepositoryImpl @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val inseatApi: InseatApi,
    private val prefManager: PreferencesManager
) : CheckoutRepository {

    override suspend fun fetchProductList(queryIds: List<Int>) = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchProducts(queryIds)
    }

    override suspend fun makeOrder(order: Order, callback: (Result<Unit>)  -> Unit) = withContext(dispatchersProvider.getIO()) {
        inseatApi.createOrder(order, callback)
    }

    override suspend fun getBasketItemsJSON() = withContext(dispatchersProvider.getIO()) {
        prefManager.read(BASKET, "")
    }

    override suspend fun getShiftId() = withContext(dispatchersProvider.getIO()) {
        (inseatApi.fetchShop() as Shop).shiftId
    }

    override suspend fun clearBasket() {
        prefManager.remove(BASKET)
    }
}