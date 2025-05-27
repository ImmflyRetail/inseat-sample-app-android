package com.immflyretail.inseat.sampleapp.orders.presentation

import androidx.lifecycle.ViewModel
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.orders.data.OrdersRepository
import com.immflyretail.inseat.sdk.api.InseatException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class OrdersScreenViewModel @Inject constructor(
    private val repository: OrdersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersScreenState>(OrdersScreenState.Loading)
    val uiState: StateFlow<OrdersScreenState> get() = _uiState

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