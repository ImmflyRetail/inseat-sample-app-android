package com.immflyretail.inseat.sampleapp.checkout.presentation

import androidx.lifecycle.ViewModel
import com.immflyretail.inseat.sampleapp.checkout.data.CheckoutRepository
import com.immflyretail.inseat.sampleapp.checkout.presentation.models.BasketItem
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract
import com.immflyretail.inseat.sdk.api.InseatException
import com.immflyretail.inseat.sdk.api.models.Order
import com.immflyretail.inseat.sdk.api.models.OrderItem
import com.immflyretail.inseat.sdk.api.models.OrderStatusEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CheckoutScreenViewModel @Inject constructor(
    private val repository: CheckoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutScreenState>(CheckoutScreenState.Loading)
    val uiState: StateFlow<CheckoutScreenState> get() = _uiState

    private val _uiAction = Channel<CheckoutScreenActions>()
    val uiAction: Flow<CheckoutScreenActions> get() = _uiAction.receiveAsFlow()

    init {
        loadData()
    }

    fun obtainEvent(event: CheckoutScreenEvent) {
        when (event) {
            is CheckoutScreenEvent.OnMakeOrderClicked -> {
                makeOrder((uiState.value as CheckoutScreenState.Data))
            }

            CheckoutScreenEvent.OnDetailsClicked -> {
                val state = uiState.value as CheckoutScreenState.Data
                _uiState.value = state.copy(isExpanded = !state.isExpanded)
            }

            is CheckoutScreenEvent.OnSeatNumberEntered -> {
                val state = uiState.value as CheckoutScreenState.Data
                _uiState.value = state.copy(seatNumber = event.seatNumber)
            }

            CheckoutScreenEvent.OnClickOrderStatus -> runCoroutine {
                _uiAction.send(CheckoutScreenActions.Navigate { navigate(ShopScreenContract.Route) })
            }

            CheckoutScreenEvent.OnBackClicked -> runCoroutine {
                _uiAction.send(CheckoutScreenActions.Navigate { popBackStack() })
            }

            CheckoutScreenEvent.OnClickKeepShopping -> runCoroutine {
                _uiAction.send(CheckoutScreenActions.Navigate {
                    navigate(ShopScreenContract.Route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }
        }
    }

    private fun makeOrder(state: CheckoutScreenState.Data) {
        _uiState.value = CheckoutScreenState.Loading
        runCoroutine {
            val createdTime = Date()
            repository.makeOrder(
                Order(
                    id = UUID.randomUUID().toString(),
                    shiftId = repository.getShiftId(),
                    totalPrice = state.total,
                    currency = state.currency,
                    items = state.items.map {
                        OrderItem(
                            itemId = it.product.itemId,
                            name = it.product.name,
                            quantity = it.quantity,
                            price = it.product.prices.first().price
                        )
                    },
                    status = OrderStatusEnum.PLACED,
                    customerSeatNumber = state.seatNumber,
                    createdAt = createdTime,
                    updatedAt = createdTime,
                )
            ) { result ->

                runCoroutine {
                    result
                        .onSuccess {
                            repository.clearBasket()
                            _uiAction.send(CheckoutScreenActions.ShowDialog(isOrderSuccess = true))
                        }
                        .onFailure {
                            _uiState.value = CheckoutScreenState.Error("Can't make order")
                            _uiAction.send(CheckoutScreenActions.ShowDialog(isOrderSuccess = false))
                        }
                }
            }
        }
    }

    private fun loadData() = runCoroutine {
        _uiState.value = CheckoutScreenState.Loading

        val selectedItems = try {
            Json.decodeFromString<Map<Int, Int>>(repository.getBasketItemsJSON()).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }

        if (selectedItems.isEmpty()) {
            _uiState.value = CheckoutScreenState.Data(
                items = emptyList(),
                total = BigDecimal.ZERO,
                currency = ""
            )
            return@runCoroutine
        }
        try {
            val basketItems =
                repository.fetchProductList(selectedItems.keys.toList()).map { item ->
                    BasketItem(selectedItems.getOrDefault(item.itemId, 0), item)
                }
            val total = basketItems.sumOf {
                it.product.prices.first().price.multiply(BigDecimal(it.quantity))
            }
            _uiState.value = CheckoutScreenState.Data(
                items = basketItems,
                total = total,
                currency = basketItems.first().product.prices.first().currency,
            )
        } catch (e: InseatException) {
            _uiState.value = CheckoutScreenState.Error("Can't get basket data")
        }
    }
}