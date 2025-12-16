package com.immflyretail.inseat.sampleapp.shop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenContract
import com.immflyretail.inseat.sampleapp.promotion_api.PromotionContract
import com.immflyretail.inseat.sampleapp.settings_api.SettingsScreenContract
import com.immflyretail.inseat.sampleapp.shop.data.ShopRepository
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopStatus
import com.immflyretail.inseat.sampleapp.shop.presentation.model.TabItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.toShopStatus
import com.immflyretail.inseat.sdk.api.InseatException
import com.immflyretail.inseat.sdk.api.models.DefaultShop
import com.immflyretail.inseat.sdk.api.models.Shop
import com.immflyretail.inseat.sdk.api.models.ShopInfo
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopScreenViewModel @Inject constructor(
    private val repository: ShopRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShopScreenState>(ShopScreenState.Loading)
    val uiState: StateFlow<ShopScreenState> get() = _uiState

    private val _uiAction = Channel<ShopScreenActions>()
    val uiAction: Flow<ShopScreenActions> get() = _uiAction.receiveAsFlow()

    private var selectedItems = mapOf<Int, Int>()
    private var shopStatus = ShopStatus.DEFAULT
    private var ordersCount = 0
    private var tabs: List<TabItem> = emptyList()
    private var selectedTab: TabItem? = null
    private var tabDataObserver: StateFlow<List<ShopItem>> = MutableStateFlow(emptyList())

    init {
        runCoroutine {
            if (repository.isMenuSelected()) {
                initShop()
            } else {
                _uiState.value = ShopScreenState.SelectMenu(repository.getAvailableMenus())
            }
        }

        runCoroutine {
            repository.getBasketItemsFlow()
                .onEach { basket ->
                    selectedItems = basket
                    val state = uiState.value
                    if (state is ShopScreenState.DataLoaded) {
                        val updatedItems = state.items.map { shopItem ->
                            val newQuantity = basket.getOrDefault(shopItem.product.itemId, 0)
                            shopItem.copy(selectedQuantity = newQuantity)
                        }

                        val newSearchResult = state.searchResult.map { shopItem ->
                            val newQuantity = basket.getOrDefault(shopItem.product.itemId, 0)
                            shopItem.copy(selectedQuantity = newQuantity)
                        }

                        _uiState.value = state.copy(
                            items = updatedItems,
                            itemsInBasket = basket.values.sumOf { count -> count },
                            searchResult = newSearchResult
                        )
                    }
                }
                .collect()
        }
    }

    fun obtainEvent(event: ShopScreenEvent) {
        when (event) {
            is ShopScreenEvent.OnAddItemClicked -> runCoroutine {
                repository.addToBasketItem(event.itemId)
            }

            is ShopScreenEvent.OnRemoveItemClicked -> runCoroutine {
                repository.removeFromBasketItem(event.itemId)
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
                        searchResult = emptyList(),
                    )
                }
                _uiAction.send(ShopScreenActions.MoveFocusToSearch)
            }

            is ShopScreenEvent.OnTabSelected -> runCoroutine {
                val currentState = _uiState.value as? ShopScreenState.DataLoaded ?: return@runCoroutine
                selectedTab = event.tab

                when (val tab = event.tab) {
                    is TabItem.CategoryTab -> {
                        tabDataObserver = repository.getProductsObserver(tab.category)
                            .map { items ->
                                items.map {
                                    ShopItem(it, selectedItems.getOrDefault(it.itemId, 0))
                                }
                            }
                            .onEach { tabItems ->
                                _uiState.value = currentState.copy(
                                    selectedTabIndex = event.selectedTabIndex,
                                    items = tabItems
                                )
                            }
                            .stateIn(viewModelScope)
                    }

                    is TabItem.PromotionTab -> {
                        _uiState.value = currentState.copy(
                            selectedTabIndex = event.selectedTabIndex,
                            items = emptyList()
                        )
                    }
                }
            }

            is ShopScreenEvent.OnSearch -> {
                val state = uiState.value as? ShopScreenState.DataLoaded ?: return
                _uiState.value = state.copy(
                    searchQuery = event.query,
                    searchResult = state.items.filter {
                        it.product.name.contains(event.query, ignoreCase = true)
                    }
                )
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

        val categoryDef = async { repository.fetchCategories() }

        try {
            updateShopStatus(repository.fetchShop())

            val newTabs = mutableListOf<TabItem>()
            newTabs.add(TabItem.PromotionTab(repository.fetchPromotions()))
            val categoriesTabs = categoryDef.await()
                .sortedBy { it.sortOrder }
                .map { TabItem.CategoryTab(it, emptyList()) }
            newTabs.addAll(categoriesTabs)
            tabs = newTabs

            selectedTab = tabs.first()

            val items = when (selectedTab) {
                is TabItem.CategoryTab -> {
                    repository.fetchProducts((selectedTab as TabItem.CategoryTab).category)
                        .map {
                            ShopItem(
                                product = it,
                                selectedItems.getOrDefault(it.itemId, 0)
                            )
                        }
                }

                else -> emptyList()
            }


            _uiState.value = ShopScreenState.DataLoaded(
                shopStatus = shopStatus,
                items = items,
                tabs = tabs,
                ordersCount = ordersCount,
                isPullToRefreshEnabled = true,
                isRefreshing = false,
                itemsInBasket = getItemsInBasketCount(),
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

        // set tabs only once, because promotions count and categories can't change during runtime
        if (tabs.isEmpty()) {
            val newTabs = mutableListOf<TabItem>()
            newTabs.add(TabItem.PromotionTab(repository.fetchPromotions()))
            val categoriesTabs = categoryDef.await()
                .sortedBy { it.sortOrder }
                .map { TabItem.CategoryTab(it, emptyList()) }
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

    private fun getItemsInBasketCount(): Int {
        return selectedItems.values.sumOf { it }
    }
}