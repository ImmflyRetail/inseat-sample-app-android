package com.immflyretail.inseat.sampleapp.shop.data

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.AUTO_REFRESH
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.BASKET
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Menu
import com.immflyretail.inseat.sdk.api.models.Product
import com.immflyretail.inseat.sdk.api.models.ShopInfo
import com.immflyretail.inseat.sdk.api.models.UserData
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ShopRepository {
    suspend fun getProductsObserver(): StateFlow<List<Product>>
    suspend fun getShopObserver(): StateFlow<ShopInfo>
    suspend fun fetchShop(): ShopInfo
    suspend fun fetchProducts(): List<Product>
    suspend fun fetchCategories(): List<Category>
    suspend fun getBasketItemsJSON(): String
    suspend fun setBasketItemsJSON(json: String)
    suspend fun selectMenu(menu: Menu)
    suspend fun isMenuSelected() : Boolean
    suspend fun getAvailableMenus() : List<Menu>
    suspend fun isAutoupdateEnabled() : Boolean
}

internal class ListRepositoryImpl @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val inseatApi: InseatApi,
    private val preferencesManager: PreferencesManager
) : ShopRepository {

    override suspend fun getProductsObserver() = withContext(dispatchersProvider.getIO()) {
        inseatApi.observeProducts()
    }

    override suspend fun getShopObserver()= withContext(dispatchersProvider.getIO()) {
        inseatApi.observeShop()
    }

    override suspend fun fetchShop() = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchShop()
    }

    override suspend fun fetchProducts(): List<Product> = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchProducts()
    }

    override suspend fun isAutoupdateEnabled(): Boolean {
        return preferencesManager.read(AUTO_REFRESH, true)
    }

    override suspend fun getBasketItemsJSON(): String {
        return preferencesManager.read(BASKET, "")
    }

    override suspend fun setBasketItemsJSON(json: String) {
        preferencesManager.write(BASKET, json)
    }

    override suspend fun selectMenu(menu: Menu) {
        inseatApi.setUserData(UserData(menu))
    }

    override suspend fun isMenuSelected() : Boolean {
        return inseatApi.fetchSelectedMenu() != null
    }

    override suspend fun getAvailableMenus(): List<Menu> = withContext(dispatchersProvider.getIO()){
        inseatApi.fetchMenus()
    }

    override suspend fun fetchCategories(): List<Category> = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchCategories()
    }
}