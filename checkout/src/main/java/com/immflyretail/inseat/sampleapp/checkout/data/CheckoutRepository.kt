package com.immflyretail.inseat.sampleapp.checkout.data

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.BASKET
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.AppliedPromotion
import com.immflyretail.inseat.sdk.api.models.CartItem
import com.immflyretail.inseat.sdk.api.models.Order
import com.immflyretail.inseat.sdk.api.models.Product
import com.immflyretail.inseat.sdk.api.models.Shop
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface CheckoutRepository {
    suspend fun fetchProductList(queryIds: List<Int>):List<Product>
    suspend fun applyPromotions(cartItems: List<CartItem>, currency: String): List<AppliedPromotion>
    suspend fun applyPromotion(promotionId: Int, cartItems: List<CartItem>, currency: String): List<AppliedPromotion>
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

    override suspend fun applyPromotions(cartItems: List<CartItem>, currency: String) = withContext(dispatchersProvider.getIO()) {
        inseatApi.applyPromotions(cartItems, currency).appliedPromotions
    }

    override suspend fun applyPromotion(promotionId: Int, cartItems: List<CartItem>, currency: String) = withContext(dispatchersProvider.getIO()) {
        val promotion = inseatApi.fetchPromotions().find { it.promotionId == promotionId } ?: return@withContext emptyList<AppliedPromotion>()
        inseatApi.applyPromotion(promotion, cartItems, currency).appliedPromotions
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