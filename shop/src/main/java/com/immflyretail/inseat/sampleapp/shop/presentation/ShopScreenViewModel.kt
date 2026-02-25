package com.immflyretail.inseat.sampleapp.shop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.navigation.popBackStackOrFinish
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenContract
import com.immflyretail.inseat.sampleapp.promotion_api.PromotionContract
import com.immflyretail.inseat.sampleapp.settings_api.SettingsScreenContract
import com.immflyretail.inseat.sampleapp.shop.data.ShopRepository
import com.immflyretail.inseat.sampleapp.shop.presentation.model.CategoryTabItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopStatus
import com.immflyretail.inseat.sampleapp.shop.presentation.model.SubcategoryTabItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.toShopStatus
import com.immflyretail.inseat.sdk.api.InseatException
import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.DefaultShop
import com.immflyretail.inseat.sdk.api.models.Product
import com.immflyretail.inseat.sdk.api.models.Promotion
import com.immflyretail.inseat.sdk.api.models.PromotionCategory
import com.immflyretail.inseat.sdk.api.models.Shop
import com.immflyretail.inseat.sdk.api.models.ShopInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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
    private var categoryTabs: List<CategoryTabItem> = emptyList()
    private var selectedCategoryTab: CategoryTabItem? = null
    private var subcategoryTabs: List<SubcategoryTabItem> = emptyList()
    private var cachedPromotions: List<Promotion> = emptyList()
    private var cachedPromotionCategories: List<PromotionCategory> = emptyList()
    private var isProcessingUpdate = false
    private var isReloadingCategories = false
    private var productsInCategoryObserverJob: Job? = null
    private var shopObserverJob: Job? = null
    private var ordersObserverJob: Job? = null
    private var basketObserverJob: Job? = null

    fun obtainEvent(event: ShopScreenEvent) {
        when (event) {
            is ShopScreenEvent.OnAddItemClicked -> runCoroutine {
                repository.addToBasketItem(event.itemId)
            }

            is ShopScreenEvent.OnRemoveItemClicked -> runCoroutine {
                repository.removeFromBasketItem(event.itemId)
            }

            is ShopScreenEvent.OnRefresh -> {
                _uiState.value =
                    (_uiState.value as ShopScreenState.DataLoaded).copy(isRefreshing = true)
                clearCache()
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

            is ShopScreenEvent.OnTabSelected -> {
                val categoryTab = event.tab
                selectedCategoryTab = categoryTab

                observeProductsInCategory(
                    parentCategory = categoryTab.category,
                    categoryIndex = event.selectedCategoryIndex,
                    subcategoryIndex = 0
                )
            }

            is ShopScreenEvent.OnSubTabSelected -> {
                val currentState = _uiState.value as? ShopScreenState.DataLoaded ?: return
                val parentCategory = selectedCategoryTab?.category ?: return

                observeProductsInCategory(
                    parentCategory = parentCategory,
                    categoryIndex = currentState.selectedCategoryIndex,
                    subcategoryIndex = event.selectedSubcategoryIndex
                )
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
                    _uiAction.send(ShopScreenActions.Navigate { popBackStackOrFinish() })
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

    init {
        runCoroutine { initShop() }

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

    private suspend fun initShop() {
        shopObserverJob?.cancel()
        ordersObserverJob?.cancel()
        basketObserverJob?.cancel()
        productsInCategoryObserverJob?.cancel()
        if (repository.isAutoupdateEnabled()) {
            autoLoadData()
        } else {
            fetchData()
        }
        observeBasketItems()
    }

    private fun observeBasketItems() {
        basketObserverJob?.cancel()
        basketObserverJob = runCoroutine {
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

    private fun fetchData() = runCoroutine {
        _uiState.value = ShopScreenState.Loading

        try {
            updateShopStatus(repository.fetchShop())

            initializeCategoriesAndSelectFirst()

            val categoryTab = selectedCategoryTab
            if (categoryTab != null) {
                observeProductsInCategory(categoryTab.category, 0, 0)
            } else {
                _uiState.value = ShopScreenState.DataLoaded(
                    shopStatus = shopStatus,
                    items = emptyList(),
                    categoryTabs = emptyList(),
                    subcategoryTabs = emptyList(),
                    ordersCount = ordersCount,
                    isPullToRefreshEnabled = true,
                    isRefreshing = false,
                    itemsInBasket = getItemsInBasketCount(),
                )
            }
        } catch (e: InseatException) {
            _uiState.value = ShopScreenState.Error(e.message ?: "Unknown error")
        }
    }

    private fun autoLoadData() = runCoroutine {
        _uiState.value = ShopScreenState.Loading

        observeShop()
        observeOrdersCount()

        if (categoryTabs.isEmpty()) {
            initializeCategoriesAndSelectFirst()
            val categoryTab = selectedCategoryTab
            if (categoryTab != null) {
                observeProductsInCategory(categoryTab.category, 0, 0)
            }
        }

        _uiState.value = ShopScreenState.DataLoaded(
            shopStatus = shopStatus,
            categoryTabs = categoryTabs,
            subcategoryTabs = subcategoryTabs,
            items = emptyList(),
            isPullToRefreshEnabled = false,
            itemsInBasket = getItemsInBasketCount(),
            ordersCount = ordersCount
        )
    }

    // ========================================
    // SHOP
    // ========================================
    private fun observeShop() {
        shopObserverJob?.cancel()
        shopObserverJob = viewModelScope.launch {
            repository.getShopObserver()
                .onEach { shopInfo ->
                    // Detect transition between Shop and DefaultShop
                    val hasShopTypeChanged =
                        (shopInfo is DefaultShop && shopStatus != ShopStatus.DEFAULT) ||
                                (shopInfo is Shop && shopStatus == ShopStatus.DEFAULT)

                    updateShopStatus(shopInfo)

                    if (hasShopTypeChanged) {
                        handleShopTypeChange()
                    }
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

    private fun handleShopTypeChange() {
        if (isProcessingUpdate) return
        isProcessingUpdate = true

        _uiState.value = ShopScreenState.Loading

        cachedPromotions = emptyList()
        cachedPromotionCategories = emptyList()

        selectedCategoryTab = null
        subcategoryTabs = emptyList()


        isProcessingUpdate = false
    }

    // ========================================
    // ORDERS
    // ========================================
    private fun observeOrdersCount() {
        ordersObserverJob?.cancel()
        ordersObserverJob = viewModelScope.launch {
            repository.fetchOrderCount()
                .onEach { count ->
                    ordersCount = count
                    (uiState.value as? ShopScreenState.DataLoaded)?.let {
                        _uiState.value = it.copy(ordersCount = ordersCount)
                    }
                }
                .collect()
        }
    }

    private fun getItemsInBasketCount(): Int {
        return selectedItems.values.sumOf { it }
    }

    // ========================================
    // PRODUCTS
    // ========================================
    private fun observeProductsInCategory(
        parentCategory: Category,
        categoryIndex: Int,
        subcategoryIndex: Int
    ) {
        productsInCategoryObserverJob?.cancel()
        productsInCategoryObserverJob = viewModelScope.launch {
            repository.getProductsObserver(parentCategory).collect { allProducts ->
                if (isReloadingCategories) return@collect

                val allPromotions = getPromotionsWithCache()
                val promoCategories = getPromotionCategoriesWithCache()
                val productIds = allProducts.map { it.itemMasterId }

                val categoryPromotions =
                    getCategoryPromotions(allPromotions, productIds, promoCategories)

                if (allProducts.isEmpty() && categoryPromotions.isEmpty()) {
                    handleEmptyCategory()
                    return@collect
                }

                val newSubcategoryTabs = calculateSubcategories(
                    parentCategory = parentCategory,
                    allProducts = allProducts,
                    allPromotions = allPromotions,
                    promotionCategories = promoCategories,
                    productIdsInCategory = productIds
                )
                subcategoryTabs = newSubcategoryTabs

                val safeSubcategoryIndex =
                    subcategoryIndex.coerceIn(0, (newSubcategoryTabs.size - 1).coerceAtLeast(0))
                val currentTab = newSubcategoryTabs.getOrNull(safeSubcategoryIndex)
                val filteredItems = filterProductsByTab(allProducts, currentTab)

                val currentState = when (val state = _uiState.value) {
                    is ShopScreenState.DataLoaded -> state
                    else -> ShopScreenState.DataLoaded(
                        shopStatus = shopStatus,
                        categoryTabs = emptyList(),
                        subcategoryTabs = emptyList(),
                        items = emptyList(),
                        isPullToRefreshEnabled = false,
                        isRefreshing = false,
                        itemsInBasket = getItemsInBasketCount(),
                        ordersCount = ordersCount
                    )
                }

                _uiState.value = currentState.copy(
                    selectedCategoryIndex = categoryIndex,
                    selectedSubcategoryIndex = safeSubcategoryIndex,
                    items = filteredItems,
                    categoryTabs = categoryTabs,
                    subcategoryTabs = ArrayList(newSubcategoryTabs),
                    isRefreshing = false
                )
            }
        }
    }

    private fun filterProductsByTab(
        allProducts: List<Product>,
        currentTab: SubcategoryTabItem?
    ): List<ShopItem> {
        return when (currentTab) {
            is SubcategoryTabItem.PromotionTab -> emptyList()
            is SubcategoryTabItem.SubcategoryTab -> {
                allProducts.filter { it.categoryId == currentTab.category.id }
                    .map { ShopItem(it, selectedItems.getOrDefault(it.itemId, 0)) }
            }

            else -> allProducts.map { ShopItem(it, selectedItems.getOrDefault(it.itemId, 0)) }
        }
    }

    // ========================================
    // CATEGORIES
    // ========================================
    private suspend fun initializeCategoriesAndSelectFirst() {
        categoryTabs = loadValidCategoryTabs()
        selectedCategoryTab = categoryTabs.firstOrNull()
    }

    private suspend fun loadValidCategoryTabs(): List<CategoryTabItem> {
        val (categories, promotions, promoCategories) = loadCategoriesData()

        return getValidCategories(categories, promotions, promoCategories)
            .map { CategoryTabItem(it, emptyList()) }
    }

    private suspend fun loadCategoriesData(): Triple<List<Category>, List<Promotion>, List<PromotionCategory>> =
        coroutineScope {
            val categoriesDef = repository.fetchCategories()
            val promotionsDef = getPromotionsWithCache()
            val promoCatsDef = getPromotionCategoriesWithCache()

            Triple(categoriesDef, promotionsDef, promoCatsDef)
        }

    private suspend fun getValidCategories(
        allCategories: List<Category>,
        allPromotions: List<Promotion>,
        promotionCategories: List<PromotionCategory>
    ): List<Category> = allCategories.filter { category ->
        val products = repository.fetchProducts(category)
        val productIdsInCategory: List<Int> = products.map { it.itemMasterId }

        val hasProducts = products.isNotEmpty()
        val hasPromotions = getCategoryPromotions(
            allPromotions = allPromotions,
            productIdsInCategory = productIdsInCategory,
            promotionCategories = promotionCategories
        ).isNotEmpty()

        hasProducts || hasPromotions
    }.sortedBy { it.sortOrder }

    private suspend fun handleEmptyCategory() {
        if (isProcessingUpdate) return
        isProcessingUpdate = true

        try {
            categoryTabs = loadValidCategoryTabs()
            selectedCategoryTab = categoryTabs.firstOrNull()

            val categoryTab = selectedCategoryTab

            if (categoryTab != null) {
                observeProductsInCategory(
                    parentCategory = categoryTab.category,
                    categoryIndex = 0,
                    subcategoryIndex = 0
                )
            } else {
                val currentState = _uiState.value as? ShopScreenState.DataLoaded
                _uiState.value = currentState?.copy(
                    categoryTabs = emptyList(),
                    subcategoryTabs = emptyList(),
                    items = emptyList(),
                    selectedCategoryIndex = 0,
                    selectedSubcategoryIndex = 0
                ) ?: ShopScreenState.Loading
            }
        } finally {
            isProcessingUpdate = false
        }
    }

    // ========================================
    // PROMOTIONS
    // ========================================
    private suspend fun getPromotionsWithCache(): List<Promotion> {
        if (cachedPromotions.isEmpty()) {
            cachedPromotions = repository.fetchPromotions()
        }
        return cachedPromotions
    }

    private suspend fun getPromotionCategoriesWithCache(): List<PromotionCategory> {
        if (cachedPromotionCategories.isEmpty()) {
            cachedPromotionCategories = repository.fetchPromotionCategories()
        }
        return cachedPromotionCategories
    }

    // ========================================
    // CACHE
    // ========================================
    private fun clearCache() {
        cachedPromotions = emptyList()
        cachedPromotionCategories = emptyList()
    }
}

private fun getCategoryPromotions(
    allPromotions: List<Promotion>,
    productIdsInCategory: List<Int>,
    promotionCategories: List<PromotionCategory>
): List<Promotion> = allPromotions.filter { promotion ->
    when (promotion.triggerType) {
        Promotion.TriggerType.PRODUCT_PURCHASE -> {
            val hasDirectProduct =
                promotion.items.any { it.itemMasterId in productIdsInCategory }
            val hasCategoryMatch = promotion.categories.any { promotionCategory ->
                promotionCategories
                    .find { it.id == promotionCategory.categoryId }
                    ?.items?.any { it in productIdsInCategory }
                    ?: false
            }
            hasDirectProduct || hasCategoryMatch
        }

        Promotion.TriggerType.SPEND_LIMIT -> {
            promotionCategories
                .find { it.id == promotion.spendLimitCategoryId }
                ?.items?.any { it in productIdsInCategory }
                ?: false
        }
    }
}

private fun calculateSubcategories(
    parentCategory: Category,
    allProducts: List<Product>,
    allPromotions: List<Promotion>,
    promotionCategories: List<PromotionCategory>,
    productIdsInCategory: List<Int>
): List<SubcategoryTabItem> {
    val newSubcategoryTabs = mutableListOf<SubcategoryTabItem>()

    val categoryPromotions =
        getCategoryPromotions(allPromotions, productIdsInCategory, promotionCategories)

    if (categoryPromotions.isNotEmpty()) {
        newSubcategoryTabs.add(SubcategoryTabItem.PromotionTab(categoryPromotions))
    }

    val validSubcategories = parentCategory.subcategories
        .filter { subcategory -> allProducts.any { it.categoryId == subcategory.id } }
        .sortedBy { it.sortOrder }
        .map { SubcategoryTabItem.SubcategoryTab(it, emptyList()) }

    newSubcategoryTabs.addAll(validSubcategories)

    return newSubcategoryTabs
}