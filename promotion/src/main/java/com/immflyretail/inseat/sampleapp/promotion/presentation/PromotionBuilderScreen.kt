package com.immflyretail.inseat.sampleapp.promotion.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.immflyretail.inseat.sampleapp.promotion.presentation.composables.SpendLimitProgressBar
import com.immflyretail.inseat.sampleapp.promotion.presentation.model.PromotionBlock
import com.immflyretail.inseat.sampleapp.promotion.presentation.model.PromotionItem
import com.immflyretail.inseat.sampleapp.promotion_api.PromotionContract
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_10
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_14
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_14_22
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_18_26
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_10
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_12_20
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_16_24
import com.immflyretail.inseat.sampleapp.theme.green
import com.immflyretail.inseat.sampleapp.theme.red
import com.immflyretail.inseat.sampleapp.theme.superLightGreen
import com.immflyretail.inseat.sampleapp.ui.AppButton
import com.immflyretail.inseat.sampleapp.ui.AppIconButton
import com.immflyretail.inseat.sampleapp.ui.AppScaffold
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sampleapp.ui.StoreClosedBanner
import com.immflyretail.inseat.sampleapp.ui.utils.IconWrapper
import java.math.BigDecimal
import com.immflyretail.inseat.sampleapp.core.resources.R as CoreR

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
    AppScaffold(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        title = (uiState as? PromotionBuilderScreenState.DataLoaded)?.title.orEmpty(),
        onBackClicked = { viewModel.obtainEvent(PromotionBuilderScreenEvent.OnBackClicked) },
    ) {
        when (uiState) {
            is PromotionBuilderScreenState.Loading -> Loading()
            is PromotionBuilderScreenState.DataLoaded -> {
                currency = uiState.currency
                MainData(uiState, viewModel::obtainEvent)
            }

            is PromotionBuilderScreenState.Error -> ErrorScreen(
                uiState.message ?: stringResource(id = R.string.promotion_builder_error_message)
            )
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
                    SpendLimitProgressBar(
                        spendLimit = triggerType.haveToSpend,
                        selectedAmount = uiState.blocks.sumOf {
                            (it as? PromotionBlock.SpendLimitBlock)?.selectedItemsPrice
                                ?: BigDecimal.ZERO
                        }
                    )
                }
            }

            if (!uiState.isShopAvailable) {
                item {
                    StoreClosedBanner(stringResource(CoreR.string.shop_has_not_opened_yet),)
                }
            }

            uiState.blocks.forEachIndexed { index, item ->
                if (index != 0) {
                    item { HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.tertiaryContainer) }
                }
                item {
                    PromotionBlock(
                        item,
                        isShopAvailable = uiState.isShopAvailable,
                        eventReceiver = eventReceiver,
                    )
                }
            }

            item {
                AppButton(
                    text = stringResource(id = R.string.promotion_builder_add_to_basket_button),
                    onClick = { eventReceiver(PromotionBuilderScreenEvent.AddToCartClicked) },
                    isEnabled = uiState.isCompleted && uiState.isShopAvailable,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PromotionInfo(
    title: String,
    savings: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer )
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PromotionBlock(
    block: PromotionBlock,
    isShopAvailable: Boolean,
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
                        text = stringResource(
                            id = R.string.promotion_builder_select_item_title,
                            block.expectedSelectedItems
                        ),
                        style = B_18_26,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    val isCompleted = block.selectedItems == block.expectedSelectedItems
                    val labelBackgroundColor: Color
                    val labelTextColor: Color

                    if (isCompleted) {
                        labelBackgroundColor = superLightGreen
                        labelTextColor = green
                    } else {
                        labelBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                        labelTextColor = MaterialTheme.colorScheme.onBackground
                    }
                    Row(
                        modifier = Modifier
                            .background(labelBackgroundColor, shape = CircleShape)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (isCompleted) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = Icons.Outlined.Check,
                                tint = green,
                                contentDescription = stringResource(id = CoreR.string.completed)
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
                            text = stringResource(id = R.string.promotion_builder_required_label),
                            style = B_10,
                            color = labelTextColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            is PromotionBlock.SpendLimitBlock -> {}
        }
        PromotionsList(
            items = block.promotionItems,
            isShopAvailable = isShopAvailable,
            eventReceiver = eventReceiver
        )
    }

}

@Composable
private fun PromotionsList(
    items: List<PromotionItem>,
    isShopAvailable: Boolean,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items.forEach { item ->
            ProductItem(
                promotionItem = item,
                isShopAvailable = isShopAvailable,
                eventReceiver = eventReceiver
            )
        }
    }
}

@Composable
private fun ProductItem(
    promotionItem: PromotionItem,
    isShopAvailable: Boolean,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val item = promotionItem.product
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
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
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = item.description,
                    style = N_12_20,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = item.prices.find { it.currency == currency }?.amount?.toPlainString() + currency,
                    style = N_12_20,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            val decodedBytes =
                Base64.decode(item.base64Image.encodeToByteArray(), Base64.DEFAULT)
            val bitmap = try {
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size).asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .background(Color.White, shape = RoundedCornerShape(size = 8.dp))
                    .padding(8.dp)
            ) {
                Image(
                    modifier = Modifier
                        .width(106.dp)
                        .height(90.dp),
                    painter = if (bitmap != null) {
                        BitmapPainter(image = bitmap)
                    } else {
                        painterResource(CoreR.drawable.im_food_placeholder)
                    },
                    contentDescription = stringResource(id = R.string.promotion_builder_item_image_content_description)
                )
            }
        }
        ProductItemStatus(
            modifier = modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd),
            item = promotionItem,
            isShopAvailable = isShopAvailable,
            eventReceiver = eventReceiver
        )
    }
}

@Composable
private fun ProductItemStatus(
    item: PromotionItem,
    isShopAvailable: Boolean,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        item.product.quantity == 0L -> {
            OutOfStockIcon(modifier.width(106.dp))
        }

        item.selectedQuantity.toLong() == item.product.quantity -> {
            LimitReachedIcon(
                selectedQuantity = item.selectedQuantity,
                itemId = item.product.itemId,
                eventReceiver = eventReceiver,
                modifier = modifier.width(106.dp)
            )
        }

        item.selectedQuantity == 0 -> {
            NotSelectedIcon(
                isShopAvailable = isShopAvailable,
                itemId = item.product.itemId,
                eventReceiver = eventReceiver,
                modifier = modifier
            )
        }

        item.selectedQuantity > 0 && item.selectedQuantity < item.product.quantity -> {
            SelectedIcon(
                selectedQuantity = item.selectedQuantity,
                itemId = item.product.itemId,
                eventReceiver = eventReceiver,
                modifier = modifier.width(106.dp)
            )
        }
    }
}


@Composable
fun OutOfStockIcon(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                text = stringResource(CoreR.string.out_of_stock),
                style = N_10,
                color = red,
                textAlign = TextAlign.Center,
            )
        }

        AppIconButton(
            icon = IconWrapper.Vector(Icons.Outlined.Add),
            onClick = { },
            isEnabled = false,
            contentDescriptionId = CoreR.string.not_selected_content_description,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun NotSelectedIcon(
    itemId: Int,
    isShopAvailable: Boolean,
    eventReceiver: (PromotionBuilderScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    AppIconButton(
        icon = IconWrapper.Vector(Icons.Outlined.Add),
        onClick = { eventReceiver(PromotionBuilderScreenEvent.OnAddItemClicked(itemId)) },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentDescriptionId = CoreR.string.add_item_content_description,
        isEnabled = isShopAvailable,
        modifier = modifier.size(32.dp)
    )
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
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppIconButton(
            icon = IconWrapper.Vector(Icons.Outlined.Remove),
            onClick = { eventReceiver(PromotionBuilderScreenEvent.OnRemoveItemClicked(itemId)) },
            contentDescriptionId = CoreR.string.remove_item_content_description,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = selectedQuantity.toString(),
            style = B_14,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        AppIconButton(
            icon = IconWrapper.Vector(Icons.Outlined.Add),
            onClick = { eventReceiver(PromotionBuilderScreenEvent.OnAddItemClicked(itemId)) },
            contentDescriptionId = CoreR.string.add_item_content_description,
            modifier = Modifier.size(32.dp)
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
                .wrapContentWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                text = stringResource(CoreR.string.limit_reached),
                style = N_10,
                color = red,
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            AppIconButton(
                icon = IconWrapper.Vector(Icons.Outlined.Remove),
                onClick = { eventReceiver(PromotionBuilderScreenEvent.OnRemoveItemClicked(itemId)) },
                contentDescriptionId = CoreR.string.remove_item_content_description,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = selectedQuantity.toString(),
                style = B_14,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            AppIconButton(
                icon = IconWrapper.Vector(Icons.Outlined.Add),
                onClick = { },
                isEnabled = false,
                contentDescriptionId = CoreR.string.add_item_content_description,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
