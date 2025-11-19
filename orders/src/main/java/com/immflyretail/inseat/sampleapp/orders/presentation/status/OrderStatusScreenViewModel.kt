package com.immflyretail.inseat.sampleapp.orders.presentation.status

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.orders.data.OrdersRepository
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sdk.api.InseatException
import com.immflyretail.inseat.sdk.api.StoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class OrderStatusScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: OrdersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderStatusScreenState>(OrderStatusScreenState.Loading)
    val uiState: StateFlow<OrderStatusScreenState> get() = _uiState

    private val _uiAction = Channel<OrderStatusScreenAction>()
    val uiAction: Flow<OrderStatusScreenAction> get() = _uiAction.receiveAsFlow()
    private val orderId: String = savedStateHandle.toRoute<OrdersScreenContract.OrderStatusRoute>().orderId


    init {
        loadData(orderId)
    }

    fun obtainEvent(event: OrderStatusScreenEvent) {
        val state = _uiState.value

        if (state !is OrderStatusScreenState.Data) return

        when (event) {
            is OrderStatusScreenEvent.OnCancelOrderClicked -> {
                _uiState.value = OrderStatusScreenState.Loading
                runCoroutine {
                    repository.cancelOrder(orderId) { result ->
                        result
                            .onSuccess {
                                _uiState.value = state
                            }
                            .onFailure {
                                _uiState.value = OrderStatusScreenState.Error(it.message.orEmpty())
                            }
                    }
                }
            }

            OrderStatusScreenEvent.OnBackClicked -> runCoroutine {
                _uiAction.send(OrderStatusScreenAction.Navigate { popBackStack() })
            }
        }
    }

    private fun loadData(orderId: String) = runCoroutine {
        _uiState.value = OrderStatusScreenState.Loading

        try {
            repository.observeOrders()
                .onEach { items ->
                    val order = items.find { it.id == orderId }
                        ?: throw StoreException("Order not found")
                    _uiState.value = OrderStatusScreenState.Data(order)
                }
                .collect()
        } catch (e: InseatException) {
            _uiState.value = OrderStatusScreenState.Error(e.message.orEmpty())
        }
    }
}