package com.immflyretail.inseat.sampleapp.shop.presentation

import androidx.lifecycle.ViewModel
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sampleapp.promotion_api.PromotionContract
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenContract
import com.immflyretail.inseat.sampleapp.settings_api.SettingsScreenContract
import com.immflyretail.inseat.sdk.api.InseatException
import com.immflyretail.inseat.sdk.api.models.DefaultShop
import com.immflyretail.inseat.sdk.api.models.Shop
import com.immflyretail.inseat.sdk.api.models.ShopInfo
import com.immflyretail.inseat.sampleapp.shop.data.ShopRepository
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopStatus
import com.immflyretail.inseat.sampleapp.shop.presentation.model.TabItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.toShopStatus
import com.immflyretail.inseat.sdk.api.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
    private var ordersCount = 0
    private var tabs: List<TabItem> = emptyList()
    private var selectedTab: TabItem? = null
    private var allProducts: List<ShopItem> = emptyList()

    init {
        runCoroutine {
            if (repository.isMenuSelected()) {
                refreshSelectedItems()
                initShop()
            } else {
                _uiState.value = ShopScreenState.SelectMenu(repository.getAvailableMenus())
            }
        }
    }

    private suspend fun refreshSelectedItems() {
        selectedItems.clear()
        selectedItems.putAll(
            try {
                Json.decodeFromString<Map<Int, Int>>(repository.getBasketItemsJSON())
            } catch (e: Exception) {
                mutableMapOf()
            }
        )
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

            ShopScreenEvent.OnCartClicked -> runCoroutine {
                _uiAction.send(ShopScreenActions.Navigate { navigate(BasketScreenContract.Route) })
            }

            ShopScreenEvent.OnOrdersClicked -> runCoroutine {
                _uiAction.send(ShopScreenActions.Navigate { navigate(OrdersScreenContract.OrdersListRoute) })
            }

            ShopScreenEvent.OnSettingsClicked -> runCoroutine {
                _uiAction.send(ShopScreenActions.Navigate { navigate(SettingsScreenContract.Route) })
            }

            ShopScreenEvent.OnSearchClicked -> runCoroutine {
                (_uiState.value as? ShopScreenState.DataLoaded)?.let { currentState ->
                    _uiState.value = currentState.copy(
                        isSearchEnabled = true,
                        searchQuery = "",
                        searchResult = emptyList()
                    )
                }
                _uiAction.send(ShopScreenActions.MoveFocusToSearch)
            }

            is ShopScreenEvent.OnTabSelected -> {
                val currentState = _uiState.value as? ShopScreenState.DataLoaded ?: return
                selectedTab = event.tab
                _uiState.value = currentState.copy(
                    selectedTabIndex = event.selectedTabIndex,
                    items = when (event.tab) {
                        is TabItem.CategoryTab -> getItemsByCategory(event.tab.category)
                        else -> emptyList()
                    }
                )
            }

            is ShopScreenEvent.OnSearch -> {
                _uiState.value = (_uiState.value as? ShopScreenState.DataLoaded)?.copy(
                    searchQuery = event.query,
                    searchResult = allProducts.filter {
                        it.product.name.contains(event.query, ignoreCase = true)
                    }
                ) ?: return
            }

            ShopScreenEvent.OnBackClicked -> runCoroutine {
                val dataState = uiState.value as? ShopScreenState.DataLoaded
                if (dataState?.isSearchEnabled == true) {
                    _uiState.value = dataState.copy(
                        searchQuery = "",
                        searchResult = emptyList(),
                        isSearchEnabled = false
                    )
                } else {
                    _uiAction.send(ShopScreenActions.Navigate { popBackStack() })
                }
            }

            is ShopScreenEvent.OnProductClicked -> runCoroutine {
                _uiAction.send(ShopScreenActions.Navigate {
                    navigate(ProductScreenContract.Route(event.itemId))
                })
            }

            is ShopScreenEvent.OnPromotionClicked -> {
                runCoroutine {
                    _uiAction.send(ShopScreenActions.Navigate {
                        navigate(PromotionContract.Route(event.promotionId))
                    })
                }
            }

            is ShopScreenEvent.OnProductUpdated -> {
                when (event.selectedAmount) {
                    0, -1 -> selectedItems.remove(event.productId)
                    else -> selectedItems[event.productId] = event.selectedAmount
                }

                (uiState.value as? ShopScreenState.DataLoaded)?.let { state ->
                    val updatedIndex = allProducts.indexOfFirst { it.product.itemId == event.productId }
                    val updatedItems = allProducts.toMutableList()
                    updatedItems[updatedIndex] =
                        updatedItems[updatedIndex].copy(selectedQuantity = event.selectedAmount)
                    allProducts = updatedItems

                    _uiState.value = state.copy(
                        itemsInBasket = getItemsInBasketCount(),
                        items = when (val tab = selectedTab) {
                            is TabItem.CategoryTab -> getItemsByCategory(tab.category)
                            else -> emptyList()
                        }
                    )
                }
            }

            ShopScreenEvent.ItemInBasketUpdated -> runCoroutine {
                refreshSelectedItems()
                allProducts = allProducts.map {
                    ShopItem(product = it.product, selectedItems.getOrDefault(it.product.itemId, 0))
                }

                (uiState.value as? ShopScreenState.DataLoaded)?.let { state ->
                    _uiState.value = state.copy(
                        itemsInBasket = getItemsInBasketCount(),
                        items = when (val tab = selectedTab) {
                            is TabItem.CategoryTab -> getItemsByCategory(tab.category)
                            else -> emptyList()
                        }
                    )
                }
            }
        }
    }

    private fun getItemsByCategory(category: Category?): List<ShopItem> {
        if (category == null) {
            return allProducts
        }
        return allProducts.filter { it.product.categoryId == category.id || category.subcategories.any { subcategory -> subcategory.id == it.product.categoryId } }
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

        val categoryDef = async { repository.fetchCategories() }

        try {
            updateShopStatus(repository.fetchShop())
            val items = repository.fetchProducts()
                .map { ShopItem(product = it, selectedItems.getOrDefault(it.itemId, 0)) }
            allProducts = items

            val newTabs = mutableListOf<TabItem>()
            newTabs.add(TabItem.PromotionTab(repository.fetchPromotions()))
            val categoriesTabs = categoryDef.await()
                .filter { getItemsByCategory(it).isNotEmpty() }
                .sortedBy { it.sortOrder }
                .map { TabItem.CategoryTab(it) }
            newTabs.addAll(categoriesTabs)
            tabs = newTabs

            selectedTab = tabs.first()

            _uiState.value = ShopScreenState.DataLoaded(
                shopStatus = shopStatus,
                items = emptyList(),
                tabs = tabs,
                ordersCount = ordersCount,
                isPullToRefreshEnabled = true,
                isRefreshing = false,
                itemsInBasket = getItemsInBasketCount()
            )
        } catch (e: InseatException) {
            _uiState.value = ShopScreenState.Error(e.message ?: "Unknown error")
        }
    }

    private fun autoLoadData() = runCoroutine {
        _uiState.value = ShopScreenState.Loading

        val categoryDef = async { repository.fetchCategories().toMutableList() }

        launch {
            repository.getShopObserver()
                .onEach { shopInfo -> updateShopStatus(shopInfo) }
                .collect()
        }

        launch {
            repository.fetchOrderCount()
                .onEach {
                    ordersCount = it
                    if (_uiState.value is ShopScreenState.DataLoaded) {
                        _uiState.value =
                            (_uiState.value as ShopScreenState.DataLoaded).copy(ordersCount = ordersCount)
                    }
                }
                .collect()
        }

        launch {
            repository.getProductsObserver()
                .map { itemDTO ->
                    itemDTO.map { ShopItem(product = it, selectedItems.getOrDefault(it.itemId, 0)) }
                }
                .onEach { items ->
                    allProducts = items

                    // set tabs only once, because promotions count and categories can't change during runtime
                    if (tabs.isEmpty()) {
                        val newTabs = mutableListOf<TabItem>()
                        newTabs.add(TabItem.PromotionTab(repository.fetchPromotions()))
                        val categoriesTabs = categoryDef.await()
                            .filter { getItemsByCategory(it).isNotEmpty() }
                            .sortedBy { it.sortOrder }
                            .map { TabItem.CategoryTab(it) }
                        newTabs.addAll(categoriesTabs)
                        tabs = newTabs

                        selectedTab = tabs.first()
                    }

                    _uiState.value = ShopScreenState.DataLoaded(
                        shopStatus = shopStatus,
                        tabs = tabs,
                        items = emptyList(),
                        isPullToRefreshEnabled = false,
                        itemsInBasket = getItemsInBasketCount(),
                        ordersCount = ordersCount
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
                ordersCount = ordersCount,
                shopStatus = shopStatus,
                itemsInBasket = getItemsInBasketCount()
            )
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
            _uiState.value = this.copy(items = items, itemsInBasket = getItemsInBasketCount())
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
            _uiState.value = this.copy(items = items, itemsInBasket = getItemsInBasketCount())
        }

        repository.setBasketItemsJSON(Json.encodeToString(selectedItems))
    }

    private fun getItemsInBasketCount(): Int {
        return selectedItems.values.sumOf { it }
    }
}