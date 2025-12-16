package com.immflyretail.inseat.sampleapp.promotion.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.promotion.R
import com.immflyretail.inseat.sampleapp.promotion.presentation.model.PromotionBlock
import com.immflyretail.inseat.sampleapp.promotion.presentation.model.PromotionItem
import com.immflyretail.inseat.sampleapp.promotion_api.PromotionContract
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.InseatButton
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_10
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14_22
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_18_26
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_10
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_12_20
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_16_24
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sdk.api.models.Money
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.min

private val statusRawHeight = 44.dp
private var currency: String = ""

fun NavGraphBuilder.promotionBuilderScreen(navController: NavController) {
    composable<PromotionContract.Route> {
        val viewModel: PromotionBuilderScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        PromotionBuilder(uiState, viewModel, navController)
    }
}

@Composable
fun PromotionBuilder(
    uiState: PromotionBuilderScreenState,
    viewModel: PromotionBuilderScreenViewModel,
    navController: NavController
) {
    Screen(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        title = (uiState as? PromotionBuilderScreenState.DataLoaded)?.title.orEmpty(),
        isBackButtonEnabled = false,
        hasTopBar = false
    ) {
        when (uiState) {
            is PromotionBuilderScreenState.Loading -> Loading()
            is PromotionBuilderScreenState.DataLoaded -> {
                currency = uiState.currency
                MainData(uiState, viewModel::obtainEvent)
            }

            is PromotionBuilderScreenState.Error -> ErrorScreen(uiState.message ?: "Error")
        }

        SingleEventEffect(viewModel.uiAction) { action ->
            when (action) {
                is PromotionBuilderScreenActions.Navigate -> navController.execute(action.lambda)
            }
        }

        BackHandler { viewModel.obtainEvent(PromotionBuilderScreenEvent.OnBackClicked) }
    }
}

@Composable
fun MainData(
    uiState: PromotionBuilderScreenState.DataLoaded,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFFFF))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier)
            Text(
                text = uiState.title,
                style = B_18_26
            )
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = "Close screen",
                modifier = Modifier.clickable { eventReceiver(PromotionBuilderScreenEvent.OnBackClicked) }
            )
        }


        LazyColumn(
            contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item { PromotionInfo(uiState.title, uiState.savings, uiState.description) }

            val triggerType = uiState.triggerType
            if (triggerType is PromotionTriggerType.SpendLimit) {
                item {
                    SpendLimitProgress(
                        spendLimit = triggerType.haveToSpend,
                        selectedAmount = uiState.blocks.sumOf {
                            (it as? PromotionBlock.SpendLimitBlock)?.selectedItemsPrice ?: BigDecimal.ZERO
                        }
                    )
                }
            }

            uiState.blocks.forEachIndexed { index, item ->
                if (index != 0) {
                    item { HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E2E2)) }
                }
                item { PromotionBlock(item, eventReceiver) }
            }

            item {
                InseatButton(
                    text = "Add to basket",
                    onClick = { eventReceiver(PromotionBuilderScreenEvent.AddToCartClicked) },
                    isEnabled = uiState.isCompleted
                )
            }
        }
    }
}

@Composable
fun PromotionInfo(title: String, savings: String, description: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFFF2F2F2))
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = B_18_26
            )
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = savings,
                style = B_18_26
            )
            Text(
                text = description,
                style = N_16_24,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun SpendLimitProgress(spendLimit: Money, selectedAmount: BigDecimal, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .padding(16.dp),
        ) {
            LinearProgressIndicator(
                progress = { min(selectedAmount.divide(spendLimit.amount, 2, RoundingMode.HALF_EVEN).toFloat(), 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFDD083A),
                trackColor = Color(0xFFE2E2E2),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            val delta = spendLimit.amount - selectedAmount

            Row(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (delta > BigDecimal.ZERO) {
                    Text(
                        text = delta.toPlainString() + spendLimit.currency,
                        style = B_14
                    )
                    Text(
                        text = stringResource(R.string.away_to_unlock_benefits),
                        style = N_14,
                        color = Color(0xFF666666)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.spend_limit_reached),
                        style = N_14,
                        color = Color(0xFF333333)
                    )

                    Image(painterResource(id = R.drawable.ic_green_compete), contentDescription = "Completed")
                }
            }

            Text(
                modifier = Modifier.align(Alignment.BottomEnd),
                text = spendLimit.amount.toPlainString() + spendLimit.currency,
                style = N_14,
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
fun PromotionBlock(
    block: PromotionBlock,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (block) {
            is PromotionBlock.ProductPurchaseBlock -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select ${block.expectedSelectedItems} item",
                        style = B_18_26,
                        color = Color(0xFF333333),
                    )

                    val isCompleted = block.selectedItems == block.expectedSelectedItems
                    val labelBackgroundColor: Color
                    val labelTextColor: Color

                    if (isCompleted) {
                        labelBackgroundColor = Color(0xFFE1F4E6)
                        labelTextColor = Color(0xFF109C42)
                    } else {
                        labelBackgroundColor = Color(0xFFE2E2E2)
                        labelTextColor = Color(0xFF333333)
                    }
                    Row(
                        modifier = Modifier
                            .height(24.dp)
                            .background(labelBackgroundColor, shape = RoundedCornerShape(24.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (isCompleted) {
                            Image(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = "Completed"
                            )
                        } else {
                            Text(
                                text = (block.expectedSelectedItems - block.selectedItems).toString(),
                                style = B_10,
                                color = labelTextColor,
                                textAlign = TextAlign.Center,
                            )
                        }

                        Text(
                            text = "Required",
                            style = B_10,
                            color = labelTextColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            is PromotionBlock.SpendLimitBlock -> {}
        }
        PromotionsList(block.promotionItems, eventReceiver)
    }

}

@Composable
private fun PromotionsList(
    items: List<PromotionItem>,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items.forEach { item ->
            ProductItem(promotionItem = item, eventReceiver = eventReceiver)
        }
    }
}

@Composable
private fun ProductItem(
    promotionItem: PromotionItem,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val item = promotionItem.product
    Box(
        modifier = modifier
            .height(106.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { eventReceiver(PromotionBuilderScreenEvent.OnAddItemClicked(item.itemId)) },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = item.name,
                    style = B_14_22,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF333333)
                )

                Text(
                    text = item.description,
                    style = N_12_20,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF666666)
                )

                Text(
                    text = item.prices.find { it.currency == currency }?.amount?.toPlainString() + currency,
                    style = N_12_20,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF333333)
                )
            }

            val decodedBytes =
                Base64.decode(item.base64Image.encodeToByteArray(), Base64.DEFAULT)
            val bitmap = try {
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size).asImageBitmap()
            } catch (e: Exception){
                e.printStackTrace()
                null
            }

            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .background(Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 8.dp))
                    .padding(8.dp)
            ) {
                Image(
                    modifier = Modifier
                        .width(106.dp)
                        .height(90.dp),
                    painter = if (bitmap != null) {
                        BitmapPainter(image = bitmap)
                    } else {
                        painterResource(R.drawable.placeholder_image)
                    },
                    contentDescription = "Product image"
                )
            }
        }
        ProductItemStatus(
            modifier = modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd),
            item = promotionItem,
            eventReceiver = eventReceiver
        )
    }
}

@Composable
private fun ProductItemStatus(
    item: PromotionItem,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        item.product.quantity == 0L -> {
            OutOfStockIcon(modifier.width(106.dp))
        }

        item.selectedQuantity.toLong() == item.product.quantity -> {
            LimitReachedIcon(
                item.selectedQuantity,
                item.product.itemId,
                eventReceiver,
                modifier.width(106.dp)
            )
        }

        item.selectedQuantity == 0 -> {
            NotSelectedIcon(
                item.product.itemId,
                eventReceiver,
                modifier
            )
        }

        item.selectedQuantity > 0 && item.selectedQuantity < item.product.quantity -> {
            SelectedIcon(
                item.selectedQuantity,
                item.product.itemId,
                eventReceiver,
                modifier.width(106.dp)
            )
        }
    }
}


@Composable
fun OutOfStockIcon(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(14.dp)
                .wrapContentWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                text = stringResource(R.string.out_of_stock),
                style = N_10,
                color = Color(0xFFD40E14),
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(12.dp),
                painter = painterResource(id = R.drawable.ic_plus_dissabled),
                contentDescription = "Not selected"
            )
        }
    }
}

@Composable
fun NotSelectedIcon(
    itemId: Int,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0xFFE2E2E2))
            .clickable { eventReceiver(PromotionBuilderScreenEvent.OnAddItemClicked(itemId)) },
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(12.dp),
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = "Not selected",
        )
    }
}

@Composable
fun SelectedIcon(
    selectedQuantity: Int,
    itemId: Int,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE2E2E2)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            modifier = Modifier
                .padding(start = 6.dp)
                .width(13.dp)
                .height(14.dp)
                .clickable { eventReceiver(PromotionBuilderScreenEvent.OnRemoveItemClicked(itemId)) },
            painter = painterResource(id = R.drawable.ic_remove),
            contentDescription = "Not selected"
        )
        Text(
            text = selectedQuantity.toString(),
            style = B_14,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
        )
        Image(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(12.dp)
                .clickable { eventReceiver(PromotionBuilderScreenEvent.OnAddItemClicked(itemId)) },
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = "Not selected"
        )
    }
}

@Composable
fun LimitReachedIcon(
    selectedQuantity: Int,
    itemId: Int,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(14.dp)
                .wrapContentWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                text = stringResource(R.string.limit_reached),
                style = N_10,
                color = Color(0xFFD40E14),
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .height(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE2E2E2)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .width(13.dp)
                    .height(14.dp)
                    .clickable { eventReceiver(PromotionBuilderScreenEvent.OnRemoveItemClicked(itemId)) },
                painter = painterResource(id = R.drawable.ic_remove),
                contentDescription = "Not selected"
            )
            Text(
                text = selectedQuantity.toString(),
                style = B_14,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
            )
            Image(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(12.dp),
                painter = painterResource(id = R.drawable.ic_plus_dissabled),
                contentDescription = "Not selected"
            )
        }
    }
}
