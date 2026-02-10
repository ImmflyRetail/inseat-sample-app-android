package com.immflyretail.inseat.sampleapp.promotion.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.promotion.data.PromotionRepository
import com.immflyretail.inseat.sampleapp.promotion.presentation.PromotionBuilderScreenActions.Navigate
import com.immflyretail.inseat.sampleapp.promotion.presentation.model.PromotionBlock
import com.immflyretail.inseat.sampleapp.promotion.presentation.model.PromotionItem
import com.immflyretail.inseat.sampleapp.promotion_api.PromotionContract
import com.immflyretail.inseat.sdk.api.InseatException
import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Money
import com.immflyretail.inseat.sdk.api.models.Promotion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PromotionBuilderScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PromotionRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<PromotionBuilderScreenState>(PromotionBuilderScreenState.Loading)
    val uiState: StateFlow<PromotionBuilderScreenState> get() = _uiState

    private val _uiAction = Channel<PromotionBuilderScreenActions>()
    val uiAction: Flow<PromotionBuilderScreenActions> get() = _uiAction.receiveAsFlow()
    private val currency = "EUR"

    init {
        val promotionId = savedStateHandle.toRoute<PromotionContract.Route>().promotionId
        fetchData(promotionId)
    }

    fun obtainEvent(event: PromotionBuilderScreenEvent) {
        when (event) {
            is PromotionBuilderScreenEvent.OnAddItemClicked -> runCoroutine {
                uiState.value.increaseSelectedQuantity(event.itemId)
            }

            is PromotionBuilderScreenEvent.OnRemoveItemClicked -> runCoroutine {
                uiState.value.decreaseSelectedQuantity(event.itemId)
            }

            PromotionBuilderScreenEvent.AddToCartClicked -> runCoroutine {
                val state =
                    uiState.value as? PromotionBuilderScreenState.DataLoaded ?: return@runCoroutine
                repository.addToBasket(
                    state.blocks
                        .map { it.promotionItems.filter { promo -> promo.selectedQuantity > 0 } }
                        .flatten()
                )
                _uiAction.send(Navigate { navigate(BasketScreenContract.Route) })
            }

            PromotionBuilderScreenEvent.OnBackClicked -> runCoroutine {
                _uiAction.send(Navigate { popBackStack() })
            }

            is PromotionBuilderScreenEvent.OnProductUpdated -> TODO()
        }
    }

    private fun fetchData(promotionId: Int) = runCoroutine {
        _uiState.value = PromotionBuilderScreenState.Loading

        try {
            // Fetch promotion, products, and categories concurrently in parallel
            val promotionDef = async { repository.fetchPromotion(promotionId) }
            val productsDef = async { repository.fetchProducts() }
            val categoriesDef = async { repository.fetchPromotionCategories() }

            val (promotion, products, promotionCategories) = Triple(
                promotionDef.await(),
                productsDef.await(),
                categoriesDef.await()
            )

            val discount = promotion.discounts.firstOrNull { it.currency == currency }
            val savings = when (promotion.benefitType) {
                Promotion.BenefitType.DISCOUNT -> when (promotion.discountType) {
                    Promotion.DiscountType.PERCENTAGE -> discount?.discount.toString() + "% OFF"
                    Promotion.DiscountType.AMOUNT -> discount?.discount.toString() + "$currency OFF"
                    Promotion.DiscountType.FIXED_PRICE -> discount?.discount.toString() + currency
                    Promotion.DiscountType.COUPON -> "Get a voucher"
                }

                Promotion.BenefitType.COUPON -> "Get a voucher"
            }

            val triggerType: PromotionTriggerType
            val blocks = mutableListOf<PromotionBlock>()

            when (promotion.triggerType) {
                Promotion.TriggerType.PRODUCT_PURCHASE -> {
                    triggerType = PromotionTriggerType.ProductPurchase

                    // iterate over all products to find those that are part of the promotion
                    products.forEach { product ->
                        // check if product is in promotion items
                        promotion.items
                            .find { it.itemMasterId == product.itemMasterId }
                            ?.let {
                                blocks.add(
                                    PromotionBlock.ProductPurchaseBlock(
                                        expectedSelectedItems = it.quantity,
                                        promotionItems = listOf(
                                            PromotionItem(
                                                product,
                                                selectedQuantity = 0
                                            )
                                        )
                                    )
                                )
                            }
                    }

                    promotion.categories.forEach { promotionCategoryMetadata ->
                        // find expected category from fetched promotion categories
                        val expectedCategory =
                            promotionCategories.find { it.id == promotionCategoryMetadata.categoryId }

                        // find all products that belong to the expected category
                        val expectedProducts =
                            products.filter {
                                it.itemMasterId in (expectedCategory?.items ?: emptyList())
                            }

                        if (expectedProducts.isNotEmpty()) {
                            blocks.add(
                                PromotionBlock.ProductPurchaseBlock(
                                    expectedSelectedItems = promotionCategoryMetadata.quantity,
                                    promotionItems = expectedProducts.map {
                                        PromotionItem(
                                            it,
                                            selectedQuantity = 0
                                        )
                                    }
                                )
                            )
                        }
                    }
                }

                Promotion.TriggerType.SPEND_LIMIT -> {
                    val spendLimit = promotion.spendLimits.find { it.currency == currency }
                    triggerType = PromotionTriggerType.SpendLimit(
                        haveToSpend = Money(
                            currency = currency,
                            amount = (spendLimit?.spendLimit ?: 0f).toBigDecimal()
                        ),
                        spent = Money(currency = currency, amount = BigDecimal.ZERO)
                    )

                    // find expected category from fetched promotion categories
                    val expectedCategory =
                        promotionCategories.find { it.id == promotion.spendLimitCategoryId }

                    // find all products that belong to the expected category
                    val expectedProducts =
                        products.filter {
                            it.itemMasterId in (expectedCategory?.items ?: emptyList())
                        }

                    if (expectedProducts.isNotEmpty()) {
                        blocks.add(
                            PromotionBlock.SpendLimitBlock(
                                selectedItemsPrice = BigDecimal.ZERO,
                                promotionItems = expectedProducts.map { PromotionItem(it, 0) }
                            )
                        )
                    }
                }
            }

            launch {
                repository.observeShopAvailableStatus().collect { isShopAvailable ->
                    _uiState.value = PromotionBuilderScreenState.DataLoaded(
                        title = promotion.name,
                        savings = savings,
                        description = promotion.discountText,
                        triggerType = triggerType,
                        blocks = blocks,
                        currency = currency,
                        isShopAvailable = isShopAvailable,
                    )
                }
            }
        } catch (e: InseatException) {
            _uiState.value = PromotionBuilderScreenState.Error(e.message ?: "Unknown error")
        }
    }


    // check if category id is equal to expected id or check if expected id is in category subcategories
    private fun Category.containsId(id: Int): Boolean =
        this.categoryId == id || this.subcategories.any { subcategory -> subcategory.id == categoryId }


    private fun PromotionBuilderScreenState.increaseSelectedQuantity(itemId: Int) =
        updateQuantity(itemId, 1)

    private fun PromotionBuilderScreenState.decreaseSelectedQuantity(itemId: Int) =
        updateQuantity(itemId, -1)

    private fun PromotionBuilderScreenState.updateQuantity(itemId: Int, modifier: Int) {
        val state = this as? PromotionBuilderScreenState.DataLoaded ?: return
        val promoBlocks = state.blocks.toMutableList()
        var clickedPromotionItem: PromotionItem? = null

        val blockForUpdate = promoBlocks.find { block ->
            block.promotionItems.find { promo -> promo.product.itemId == itemId }
                ?.let { clickedPromotionItem = it } != null
        } ?: return
        clickedPromotionItem ?: return

        val newPromotionItems = blockForUpdate.promotionItems.map { promoItem ->
            if (promoItem.product.itemId == itemId) {
                promoItem.copy(selectedQuantity = promoItem.selectedQuantity + modifier)
            } else {
                promoItem
            }
        }

        val newPromotionBlock: PromotionBlock
        when (blockForUpdate) {
            is PromotionBlock.ProductPurchaseBlock -> {
                if (blockForUpdate.selectedItems < blockForUpdate.expectedSelectedItems) {
                    newPromotionBlock = blockForUpdate.copy(
                        selectedItems = blockForUpdate.selectedItems + modifier,
                        promotionItems = newPromotionItems
                    )
                } else {
                    return
                }
            }

            is PromotionBlock.SpendLimitBlock -> {
                val price =
                    clickedPromotionItem.product.prices.find { it.currency == currency }?.amount
                        ?: BigDecimal.ZERO
                newPromotionBlock = blockForUpdate.copy(
                    selectedItemsPrice = blockForUpdate.selectedItemsPrice + (price * modifier.toBigDecimal()),
                    promotionItems = newPromotionItems
                )
            }
        }

        promoBlocks.set(index = promoBlocks.indexOf(blockForUpdate), element = newPromotionBlock)

        val isComplete = when (triggerType) {
            PromotionTriggerType.ProductPurchase -> {
                promoBlocks.find { block ->
                    when (block) {
                        is PromotionBlock.ProductPurchaseBlock -> block.selectedItems < block.expectedSelectedItems
                        else -> false
                    }
                } == null
            }

            is PromotionTriggerType.SpendLimit -> {
                val selectedAmount = promoBlocks.sumOf { block ->
                    block.promotionItems.sumOf { promotionItem ->
                        val price =
                            promotionItem.product.prices.find { it.currency == currency }?.amount
                                ?: BigDecimal.ZERO

                        price.multiply(promotionItem.selectedQuantity.toBigDecimal())
                    }
                }
                triggerType.haveToSpend.amount <= selectedAmount
            }
        }

        _uiState.value = state.copy(blocks = promoBlocks, isCompleted = isComplete)
    }
}