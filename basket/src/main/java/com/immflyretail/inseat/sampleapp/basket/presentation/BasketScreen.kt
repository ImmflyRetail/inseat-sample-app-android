package com.immflyretail.inseat.sampleapp.basket.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.immflyretail.inseat.sampleapp.basket.R
import com.immflyretail.inseat.sampleapp.basket.presentation.model.BasketItem
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.core.extension.toLocalizedMoney
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_14
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_14_22
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_18_26
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_22_30
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_10
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_12_20
import com.immflyretail.inseat.sampleapp.theme.green
import com.immflyretail.inseat.sampleapp.theme.red
import com.immflyretail.inseat.sampleapp.ui.AppButton
import com.immflyretail.inseat.sampleapp.ui.AppIconButton
import com.immflyretail.inseat.sampleapp.ui.AppScaffold
import com.immflyretail.inseat.sampleapp.ui.ButtonStyle
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sampleapp.ui.utils.IconWrapper
import com.immflyretail.inseat.sdk.api.models.AppliedPromotion
import com.immflyretail.inseat.sdk.api.models.Money
import java.math.BigDecimal
import com.immflyretail.inseat.sampleapp.core.resources.R as CoreR

fun NavGraphBuilder.basketScreen(navController: NavController) {
    composable<BasketScreenContract.Route> {
        val viewModel: BasketScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        BasketScreen(
            uiState = uiState,
            viewModel = viewModel,
            navController = navController
        )
    }
}

@Composable
private fun BasketScreen(
    uiState: BasketScreenState,
    viewModel: BasketScreenViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    AppScaffold(
        modifier = modifier,
        title = stringResource(id = R.string.basket_screen_title),
        onBackClicked = { viewModel.obtainEvent(BasketScreenEvent.OnBackClicked) },
    ) {
        when (uiState) {
            is BasketScreenState.DataLoaded -> ContentScreen(uiState, viewModel)

            is BasketScreenState.Error -> ErrorScreen(uiState.message)
            BasketScreenState.Loading -> Loading()
        }

        SingleEventEffect(viewModel.uiAction) { action ->
            when (action) {
                is BasketScreenActions.Navigate -> navController.execute(action.lambda)
            }
        }

        BackHandler { viewModel.obtainEvent(BasketScreenEvent.OnBackClicked) }
    }
}

@Composable
private fun ContentScreen(
    uiState: BasketScreenState.DataLoaded,
    viewModel: BasketScreenViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ProductSummary(modifier, uiState, viewModel)

        FooterButtons(viewModel, uiState)
    }
}

@Composable
private fun ColumnScope.ProductSummary(
    modifier: Modifier,
    uiState: BasketScreenState.DataLoaded,
    viewModel: BasketScreenViewModel
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, end = 16.dp)
            .weight(1f),
    ) {
        Text(
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
            text = stringResource(CoreR.string.summary),
            style = B_22_30,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items = uiState.items, itemContent = { item ->
                ListItem(item = item, eventReceiver = viewModel::obtainEvent)
            })
            item {
                SummaryBlock(uiState.subtotal, uiState.appliedPromotions)
            }
        }
    }
}

@Composable
private fun ListItem(
    item: BasketItem,
    eventReceiver: (BasketScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(106.dp)
            .background(MaterialTheme.colorScheme.background)
            .clickable { eventReceiver(BasketScreenEvent.OnItemClicked(item.product.itemId)) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(122.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.BottomEnd
            ) {
                val decodedBytes =
                    Base64.decode(item.product.base64Image.encodeToByteArray(), Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    painter = if (bitmap != null) {
                        BitmapPainter(image = bitmap.asImageBitmap())
                    } else {
                        painterResource(CoreR.drawable.im_no_image)
                    },
                    contentDescription = stringResource(id = CoreR.string.image_content_description)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .wrapContentWidth()
                        .weight(1f),
                ) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .wrapContentWidth(),
                        text = item.product.name,
                        style = B_14_22,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    when {
                        item.quantity.toLong() == item.product.quantity -> LimitReachedIcon(
                            selectedQuantity = item.quantity,
                            itemId = item.product.itemId,
                            eventReceiver = eventReceiver,
                        )

                        item.quantity > 0 -> NormalIcon(
                            selectedQuantity = item.quantity,
                            itemId = item.product.itemId,
                            eventReceiver = eventReceiver,
                        )
                    }
                }

                val priceData = item.product.prices.first()
                Text(
                    modifier = Modifier
                        .wrapContentWidth()
                        .weight(0.3f)
                        .padding(bottom = 30.dp),

                    text = priceData.amount.toString() + " " + priceData.currency,
                    style = N_12_20,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
fun NormalIcon(
    selectedQuantity: Int,
    itemId: Int,
    eventReceiver: (BasketScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(106.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppIconButton(
            icon = IconWrapper.Vector(Icons.Outlined.Remove),
            onClick = { eventReceiver(BasketScreenEvent.OnRemoveItemClicked(itemId)) },
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
            onClick = { eventReceiver(BasketScreenEvent.OnAddItemClicked(itemId)) },
            contentDescriptionId = CoreR.string.add_item_content_description,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun LimitReachedIcon(
    selectedQuantity: Int,
    itemId: Int,
    eventReceiver: (BasketScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(24.dp)
            .width(106.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppIconButton(
                icon = IconWrapper.Vector(Icons.Outlined.Remove),
                onClick = { eventReceiver(BasketScreenEvent.OnRemoveItemClicked(itemId)) },
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
                isEnabled = false,
                onClick = { },
                contentDescriptionId = CoreR.string.add_item_content_description,
                modifier = Modifier.size(32.dp)
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .height(14.dp)
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
    }
}

@Composable
fun SummaryBlock(
    subtotal: Money,
    appliedPromotions: List<AppliedPromotion>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 24.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(CoreR.string.subtotal),
                style = B_14,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtotal.amount.toLocalizedMoney() + " " + subtotal.currency,
                style = B_14,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Right,
            )
        }

        if (appliedPromotions.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                thickness = 1.dp
            )

            appliedPromotions.map { appliedPromotion ->
                AppliedPromotionItem(appliedPromotion)
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                thickness = 1.dp
            )
        }

        val discount = appliedPromotions.sumOf {
            when (val type = it.benefitType) {
                is AppliedPromotion.BenefitType.Coupon -> BigDecimal.ZERO
                is AppliedPromotion.BenefitType.Discount -> type.totalSavings.amount
            }
        }
        val total = subtotal.amount - discount
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(CoreR.string.total),
                style = B_18_26,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = total.toLocalizedMoney() + " " + subtotal.currency,
                style = B_18_26,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Right,
            )
        }
    }
}

@Composable
fun AppliedPromotionItem(appliedPromotion: AppliedPromotion, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = appliedPromotion.promotion.name,
            style = B_14,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (val type = appliedPromotion.benefitType) {
                is AppliedPromotion.BenefitType.Coupon -> "Coupon(${type.couponId})"
                is AppliedPromotion.BenefitType.Discount -> "-${type.totalSavings.amount.toLocalizedMoney()} ${type.totalSavings.currency}"
            },
            style = B_14,
            color = green,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun FooterButtons(
    viewModel: BasketScreenViewModel,
    uiState: BasketScreenState.DataLoaded
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppButton(
            style = ButtonStyle.Flat,
            text = stringResource(id = R.string.basket_add_more),
            onClick = { viewModel.obtainEvent(BasketScreenEvent.OnAddMoreClicked) },
            leadingIcon = IconWrapper.Vector(Icons.Outlined.Add),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )

        AppButton(
            text = stringResource(id = R.string.basket_checkout),
            isEnabled = uiState.items.isNotEmpty(),
            onClick = { viewModel.obtainEvent(BasketScreenEvent.OnCheckoutClicked) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}
