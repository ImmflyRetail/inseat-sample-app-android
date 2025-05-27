package com.immflyretail.inseat.sampleapp.basket.presentation.basket

import androidx.lifecycle.ViewModel
import com.immflyretail.inseat.sampleapp.basket.data.BasketRepository
import com.immflyretail.inseat.sampleapp.basket.presentation.basket.model.BasketItem
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sdk.api.InseatException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class BasketScreenViewModel @Inject constructor(
    private val repository: BasketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BasketScreenState>(BasketScreenState.Loading)
    val uiState: StateFlow<BasketScreenState> get() = _uiState

    private var selectedItems = mutableMapOf<Int, Int>()

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
        }
    }

    private fun loadData() = runCoroutine {
        _uiState.value = BasketScreenState.Loading

        selectedItems = try {
            Json.decodeFromString<Map<Int, Int>>(repository.getBasketItemsJSON()).toMutableMap()
        } catch (e: Exception){
            mutableMapOf()
        }

        if (selectedItems.isEmpty()) {
            _uiState.value = BasketScreenState.DataLoaded(
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
            val total = basketItems.sumOf { it.product.prices.first().price }
            _uiState.value = BasketScreenState.DataLoaded(
                basketItems,
                total,
                basketItems.first().product.prices.first().currency
            )
        } catch (e: InseatException) {
            _uiState.value = BasketScreenState.Error("Can't get basket data")
        }
    }

    private fun BasketScreenState.increaseSelectedQuantity(itemId: Int) {
        if (this !is BasketScreenState.DataLoaded) return

        val items = items.toMutableList()
        val updatedIndex = items.indexOfFirst {  it.product.itemId == itemId }
        if (updatedIndex != -1) {
            val newQuantity = items[updatedIndex].quantity + 1
            items[updatedIndex] = items[updatedIndex].copy(quantity = newQuantity)
            selectedItems[itemId] = newQuantity
            val total = items.sumOf { it.product.prices.first().price }
            _uiState.value = this.copy(items = items, total = total)
        }
    }

    private fun BasketScreenState.decreaseSelectedQuantity(itemId: Int) {
        if (this !is BasketScreenState.DataLoaded) return

        val items = items.toMutableList()
        val updatedIndex = items.indexOfFirst {  it.product.itemId == itemId }
        if (updatedIndex != -1) {
            val newQuantity = items[updatedIndex].quantity - 1
            items[updatedIndex] = items[updatedIndex].copy(quantity = newQuantity)
            if (newQuantity <= 0) {
                selectedItems.remove(itemId)
                items.removeAt(updatedIndex)
            } else {
                selectedItems[itemId] = newQuantity
            }
            val total = items.sumOf { it.product.prices.first().price }
            _uiState.value = this.copy(items = items, total = total)
        }
    }

}