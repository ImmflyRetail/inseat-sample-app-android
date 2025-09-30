package com.immflyretail.inseat.sampleapp.basket.presentation.basket

import androidx.lifecycle.ViewModel
import com.immflyretail.inseat.sampleapp.basket.data.BasketRepository
import com.immflyretail.inseat.sampleapp.basket.presentation.basket.model.BasketItem
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenResultKey
import com.immflyretail.inseat.sampleapp.checkout_api.CheckoutScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenContract
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract
import com.immflyretail.inseat.sdk.api.InseatException
import com.immflyretail.inseat.sdk.api.models.CartItem
import com.immflyretail.inseat.sdk.api.models.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class BasketScreenViewModel @Inject constructor(
    private val repository: BasketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BasketScreenState>(BasketScreenState.Loading)
    val uiState: StateFlow<BasketScreenState> get() = _uiState

    private val _uiAction = Channel<BasketScreenActions>()
    val uiAction: Flow<BasketScreenActions> get() = _uiAction.receiveAsFlow()

    private var selectedItems = mutableMapOf<Int, Int>()
    private var selectedItemsInitValue = mutableMapOf<Int, Int>()

    private var currency: String = "EUR"

    init {
        loadData()
    }

    fun obtainEvent(event: BasketScreenEvent) {
        when (event) {
            is BasketScreenEvent.OnAddItemClicked -> {
                uiState.value.increaseSelectedQuantity(event.itemId)
                runCoroutine {
                    repository.setBasketItemsJSON(Json.encodeToString(selectedItems))
                }
            }

            is BasketScreenEvent.OnRemoveItemClicked -> {
                uiState.value.decreaseSelectedQuantity(event.itemId)
                runCoroutine { repository.setBasketItemsJSON(Json.encodeToString(selectedItems)) }
            }

            BasketScreenEvent.OnAddMoreClicked -> runCoroutine {
                _uiAction.send(BasketScreenActions.Navigate { navigate(ShopScreenContract.Route) })
            }

            BasketScreenEvent.OnBackClicked -> runCoroutine {
                _uiAction.send(BasketScreenActions.Navigate {
                    if (selectedItemsInitValue != selectedItems) {
                        previousBackStackEntry?.savedStateHandle?.set(
                            BasketScreenResultKey.PRODUCTS_IN_BASKET_REFRESHED.name, true
                        )
                    }

                    popBackStack()
                })
            }

            BasketScreenEvent.OnCheckoutClicked -> runCoroutine {
                _uiAction.send(BasketScreenActions.Navigate { navigate(CheckoutScreenContract.Route) })
            }

            is BasketScreenEvent.OnItemClicked -> runCoroutine {
                _uiAction.send(
                    BasketScreenActions.Navigate { navigate(ProductScreenContract.Route(event.itemId)) }
                )
            }
        }
    }

    private fun loadData() = runCoroutine {
        _uiState.value = BasketScreenState.Loading

        selectedItemsInitValue = try {
            Json.decodeFromString<Map<Int, Int>>(repository.getBasketItemsJSON()).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }

        selectedItems = selectedItemsInitValue.toMutableMap()
        if (selectedItems.isEmpty()) {
            _uiState.value = BasketScreenState.DataLoaded(
                items = emptyList(),
                subtotal = Money("EUR", BigDecimal.ZERO),
                appliedPromotions = emptyList()
            )
            return@runCoroutine
        }
        try {
            val basketItems = repository.fetchProductList(selectedItems.keys.toList())
                .map { item ->
                    BasketItem(selectedItems.getOrDefault(item.itemId, 0), item)
                }
            currency = basketItems.first().product.prices.first().currency
            updateBasket(basketItems)
        } catch (e: InseatException) {
            _uiState.value = BasketScreenState.Error("Can't get basket data")
        }
    }

    private fun BasketScreenState.increaseSelectedQuantity(itemId: Int) {
        if (this !is BasketScreenState.DataLoaded) return

        val items = items.toMutableList()
        val updatedIndex = items.indexOfFirst { it.product.itemId == itemId }
        if (updatedIndex != -1) {
            val newQuantity = items[updatedIndex].quantity + 1
            items[updatedIndex] = items[updatedIndex].copy(quantity = newQuantity)
            selectedItems[itemId] = newQuantity
            updateBasket(items)
        }
    }

    private fun BasketScreenState.decreaseSelectedQuantity(itemId: Int) {
        if (this !is BasketScreenState.DataLoaded) return

        val items = items.toMutableList()
        val updatedIndex = items.indexOfFirst { it.product.itemId == itemId }
        if (updatedIndex != -1) {
            val newQuantity = items[updatedIndex].quantity - 1
            items[updatedIndex] = items[updatedIndex].copy(quantity = newQuantity)
            if (newQuantity <= 0) {
                selectedItems.remove(itemId)
                items.removeAt(updatedIndex)
            } else {
                selectedItems[itemId] = newQuantity
            }
            updateBasket(items)
        }
    }

    private fun updateBasket(items: List<BasketItem>) = runCoroutine {
        val total = items.sumOf {
            val price = it.product.prices.find { price -> price.currency == currency }?.amount ?: BigDecimal.ZERO
            price.multiply(BigDecimal(it.quantity))
        }

        val appliedPromotions = repository.applyPromotions(
            items.map {
                CartItem(
                    id = it.product.itemId,
                    masterId = it.product.itemMasterId,
                    name = it.product.name,
                    quantity = it.quantity,
                    prices = it.product.prices
                )
            },
            currency = currency
        )

        _uiState.value = BasketScreenState.DataLoaded(
            items = items,
            subtotal = Money(currency, total),
            appliedPromotions = appliedPromotions
        )
    }
}