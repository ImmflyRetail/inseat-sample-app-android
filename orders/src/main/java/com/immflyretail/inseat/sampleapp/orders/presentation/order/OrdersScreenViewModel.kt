package com.immflyretail.inseat.sampleapp.orders.presentation.order

import androidx.lifecycle.ViewModel
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.orders.data.OrdersRepository
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sdk.api.InseatException
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
class OrdersScreenViewModel @Inject constructor(
    private val repository: OrdersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersScreenState>(OrdersScreenState.Loading)
    val uiState: StateFlow<OrdersScreenState> get() = _uiState

    private val _uiAction = Channel<OrderScreenAction>()
    val uiAction: Flow<OrderScreenAction> get() = _uiAction.receiveAsFlow()

    init {
        loadData()
    }

    fun obtainEvent(event: OrdersScreenEvent) {
        val state = _uiState.value

        if (state !is OrdersScreenState.Data) return

        when (event) {
            is OrdersScreenEvent.OnCancelOrderClicked -> {
                _uiState.value = OrdersScreenState.Loading
                runCoroutine {
                    repository.cancelOrder(event.orderId) { result ->
                        result
                            .onSuccess {
                                _uiState.value = state
                            }
                            .onFailure {
                                _uiState.value = OrdersScreenState.Error(it.message.orEmpty())
                            }
                    }
                }
            }

            OrdersScreenEvent.OnBackClicked -> runCoroutine { _uiAction.send(OrderScreenAction.Navigate { popBackStack() }) }
            is OrdersScreenEvent.OnDetailsClicked -> runCoroutine {
                _uiAction.send(OrderScreenAction.Navigate {
                    navigate(OrdersScreenContract.OrderStatusRoute(event.orderId))
                })
            }
        }
    }

    private fun loadData() = runCoroutine {
        _uiState.value = OrdersScreenState.Loading

        try {
            repository.observeOrders()
                .onEach { items -> _uiState.value = OrdersScreenState.Data(items) }
                .collect()
        } catch (e: InseatException) {
            _uiState.value = OrdersScreenState.Error(e.message.orEmpty())
        }
    }
}