package com.immflyretail.inseat.sampleapp.shop.presentation

import androidx.lifecycle.ViewModel
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sdk.api.InseatException
import com.immflyretail.inseat.sdk.api.models.DefaultShop
import com.immflyretail.inseat.sdk.api.models.Shop
import com.immflyretail.inseat.sdk.api.models.ShopInfo
import com.immflyretail.inseat.sampleapp.shop.data.ShopRepository
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopStatus
import com.immflyretail.inseat.sampleapp.shop.presentation.model.toShopStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ShopScreenViewModel @Inject constructor(
    private val repository: ShopRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShopScreenState>(ShopScreenState.Loading)
    val uiState: StateFlow<ShopScreenState> get() = _uiState

    private val _uiAction = Channel<ShopScreenActions>()
    val uiAction: Flow<ShopScreenActions> get() = _uiAction.receiveAsFlow()

    private val selectedItems = mutableMapOf<Int, Int>()
    private var shopStatus = ShopStatus.DEFAULT


    init {
        runCoroutine {
            if (repository.isMenuSelected()) {
                selectedItems.putAll(
                    try {
                        Json.decodeFromString<Map<Int, Int>>(repository.getBasketItemsJSON())
                    } catch (e: Exception) {
                        mutableMapOf()
                    }
                )

                initShop()
            } else {
                _uiState.value = ShopScreenState.SelectMenu(repository.getAvailableMenus())
            }
        }
    }

    fun obtainEvent(event: ShopScreenEvent) {
        when (event) {
            is ShopScreenEvent.OnAddItemClicked -> runCoroutine {
                uiState.value.increaseSelectedQuantity(event.itemId)
            }

            is ShopScreenEvent.OnRemoveItemClicked -> runCoroutine {
                uiState.value.decreaseSelectedQuantity(event.itemId)
            }


            is ShopScreenEvent.OnMenuSelected -> runCoroutine {
                repository.selectMenu(event.menu)
                initShop()
            }

            is ShopScreenEvent.OnRefresh -> {
                _uiState.value =
                    (_uiState.value as ShopScreenState.DataLoaded).copy(isRefreshing = true)
                fetchData()
            }

            ShopScreenEvent.ClickOnCategories -> runCoroutine {
                _uiState.value =
                    (_uiState.value as ShopScreenState.DataLoaded).copy(categories = repository.fetchCategories())
            }

            ShopScreenEvent.CloseCategories -> {
                _uiState.value =
                    (_uiState.value as ShopScreenState.DataLoaded).copy(categories = null)
            }

            ShopScreenEvent.OnCartClicked -> runCoroutine {
                _uiAction.send(ShopScreenActions.Navigate { navigate(BasketScreenContract.Route) })
            }
        }
    }

    private suspend fun initShop() {
        if (repository.isAutoupdateEnabled()) {
            autoLoadData()
        } else {
            fetchData()
        }
    }

    private fun fetchData() = runCoroutine {
        _uiState.value = ShopScreenState.Loading

        try {
            updateShopStatus(repository.fetchShop())
            val items = repository.fetchProducts()
                .map { ShopItem(product = it, selectedItems.getOrDefault(it.itemId, 0)) }
            _uiState.value = ShopScreenState.DataLoaded(
                shopStatus,
                items,
                isPullToRefreshEnabled = true,
                isRefreshing = false,
                itemsInBasket = selectedItems.values.sumOf { it }
            )
        } catch (e: InseatException) {
            _uiState.value = ShopScreenState.Error(e.message ?: "Unknown error")
        }
    }

    private fun autoLoadData() = runCoroutine {
        _uiState.value = ShopScreenState.Loading

        launch {
            repository.getShopObserver()
                .onEach { shopInfo -> updateShopStatus(shopInfo) }
                .collect()
        }

        launch {
            repository.getProductsObserver()
                .map { itemDTO ->
                    itemDTO.map { ShopItem(product = it, selectedItems.getOrDefault(it.itemId, 0)) }
                }
                .onEach { items ->
                    _uiState.value = ShopScreenState.DataLoaded(
                        shopStatus,
                        items,
                        isPullToRefreshEnabled = false,
                        itemsInBasket = selectedItems.values.sumOf { it }
                    )
                }
                .collect()
        }
    }

    private fun updateShopStatus(shopInfo: ShopInfo) {
        shopStatus = when (shopInfo) {
            is Shop -> shopInfo.status.toShopStatus()
            is DefaultShop -> ShopStatus.DEFAULT
        }

        val state = _uiState.value
        if (state is ShopScreenState.DataLoaded) {
            _uiState.value = state.copy(
                shopStatus = shopStatus,
                itemsInBasket = selectedItems.values.sumOf { it })
        }
    }

    private suspend fun ShopScreenState.increaseSelectedQuantity(itemId: Int) {
        if (this !is ShopScreenState.DataLoaded) return

        val items = items.toMutableList()
        val updatedIndex = items.indexOfFirst { it.product.itemId == itemId }

        if (updatedIndex != -1) {
            val newQuantity = items[updatedIndex].selectedQuantity + 1
            items[updatedIndex] = items[updatedIndex].copy(selectedQuantity = newQuantity)
            selectedItems[itemId] = newQuantity
            _uiState.value =
                this.copy(items = items, itemsInBasket = selectedItems.values.sumOf { it })
        }

        repository.setBasketItemsJSON(Json.encodeToString(selectedItems))
    }

    private suspend fun ShopScreenState.decreaseSelectedQuantity(itemId: Int) {
        if (this !is ShopScreenState.DataLoaded) return

        val items = items.toMutableList()
        val updatedIndex = items.indexOfFirst { it.product.itemId == itemId }

        if (updatedIndex != -1) {
            val newQuantity = items[updatedIndex].selectedQuantity - 1
            items[updatedIndex] = items[updatedIndex].copy(selectedQuantity = newQuantity)
            if (newQuantity <= 0) {
                selectedItems.remove(itemId)
            } else {
                selectedItems[itemId] = newQuantity
            }
            _uiState.value =
                this.copy(items = items, itemsInBasket = selectedItems.values.sumOf { it })
        }

        repository.setBasketItemsJSON(Json.encodeToString(selectedItems))
    }
}