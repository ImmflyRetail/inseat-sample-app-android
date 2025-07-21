package com.immflyretail.inseat.sampleapp.product.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.immflyretail.inseat.sampleapp.checkout_api.CheckoutScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.product.data.ProductRepository
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenContract
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenResultKey
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenResult
import com.immflyretail.inseat.sdk.api.InseatException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductScreenState>(ProductScreenState.Loading)
    val uiState: StateFlow<ProductScreenState> get() = _uiState

    private val _uiAction = Channel<ProductScreenActions>()
    val uiAction: Flow<ProductScreenActions> get() = _uiAction.receiveAsFlow()

    private val productId: Int = savedStateHandle.toRoute<ProductScreenContract.Route>().id
    private var selectedAmount: Int = 0
    private var initSelectedAmount: Int = 0

    init {
        loadData()
    }

    fun obtainEvent(event: ProductScreenEvent) {
        val state = _uiState.value
        when (event) {
            ProductScreenEvent.OnAddItemClicked -> runCoroutine {
                if (state !is ProductScreenState.Data) return@runCoroutine

                selectedAmount = state.selectedAmount + 1
                _uiState.value = state.copy(selectedAmount = selectedAmount)
                repository.updateBasketItem(productId, selectedAmount)
            }

            ProductScreenEvent.OnBackClicked -> runCoroutine {
                _uiAction.send(ProductScreenActions.Navigate {
                    if (initSelectedAmount != selectedAmount) {
                        previousBackStackEntry?.savedStateHandle?.set(
                            ProductScreenResultKey.REFRESHED_PRODUCT.name,
                            ProductScreenResult(productId, selectedAmount)
                        )
                    }
                    popBackStack()
                })
            }

            ProductScreenEvent.OnConfirmClicked -> runCoroutine {
                _uiAction.send(ProductScreenActions.Navigate { navigate(CheckoutScreenContract.Route) })
            }

            ProductScreenEvent.OnRemoveItemClicked -> runCoroutine {
                if (state !is ProductScreenState.Data) return@runCoroutine

                selectedAmount = state.selectedAmount - 1
                _uiState.value = state.copy(selectedAmount = selectedAmount)
                repository.updateBasketItem(productId, selectedAmount)
            }
        }
    }

    private fun loadData() = runCoroutine {
        _uiState.value = ProductScreenState.Loading
        try {
            initSelectedAmount = repository.getSelectedAmount(productId)
            selectedAmount = initSelectedAmount
            val product = repository.fetchProduct(productId)

            launch {
                repository.observeShopAvailableStatus().collect { isAvailable ->
                    _uiState.value = ProductScreenState.Data(
                        product = product,
                        selectedAmount = selectedAmount,
                        isShopAvailable = isAvailable,
                    )
                }
            }

        } catch (e: InseatException) {
            _uiState.value = ProductScreenState.Error("Can't get Product data")
        }
    }
}