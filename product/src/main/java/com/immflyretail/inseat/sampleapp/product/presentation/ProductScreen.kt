package com.immflyretail.inseat.sampleapp.product.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.core.extension.toBitmapPainter
import com.immflyretail.inseat.sampleapp.product.R
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenContract
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.InseatButton
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_18
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_22_30
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_10
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_16_24
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_18_26
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sdk.api.models.Product

fun NavGraphBuilder.productScreen(navController: NavController) {
    composable<ProductScreenContract.Route> {
        val viewModel: ProductScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ProductScreen(
            uiState = uiState,
            navController = navController,
            viewModel = viewModel
        )
    }
}

@Composable
private fun ProductScreen(
    uiState: ProductScreenState,
    navController: NavController,
    viewModel: ProductScreenViewModel,
    modifier: Modifier = Modifier
) {
    Screen(
        modifier = modifier,
        title = "",
        toolbarItem = {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { viewModel.obtainEvent(ProductScreenEvent.OnBackClicked) }
                    .focusable(),
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Close Icon"
            )
        },
        onBackClicked = { viewModel.obtainEvent(ProductScreenEvent.OnBackClicked) },
        isBackButtonEnabled = false
    ) {

        when (uiState) {
            is ProductScreenState.Data -> ContentScreen(
                uiState = uiState,
                eventReceiver = viewModel::obtainEvent,
            )

            is ProductScreenState.Error -> ErrorScreen(uiState.message)
            ProductScreenState.Loading -> Loading()
        }

        SingleEventEffect(viewModel.uiAction) { action ->
            when (action) {
                is ProductScreenActions.Navigate -> navController.execute(action.lambda)
            }
        }

        BackHandler { viewModel.obtainEvent(ProductScreenEvent.OnBackClicked) }
    }
}

@Composable
private fun ContentScreen(
    uiState: ProductScreenState.Data,
    modifier: Modifier = Modifier,
    eventReceiver: (ProductScreenEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Image(
            modifier = Modifier
                .height(242.dp)
                .fillMaxWidth()
                .padding(8.dp),
            contentScale = ContentScale.Fit,
            painter = uiState.product.base64Image.toBitmapPainter(R.drawable.no_image),
            contentDescription = "Image"
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            text = uiState.product.name,
            style = B_22_30,
            color = Color(0xFF333333),
        )

        val priceData = uiState.product.prices.first()
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp),
            text = priceData.amount.toString() + " " + priceData.currency,
            style = N_18_26,
            color = Color(0xFF333333),
        )

        Box(
            modifier = modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF2F2F2)),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = uiState.product.description,
                style = N_16_24,
                color = Color(0xFF666666),
            )
        }

        if (uiState.isShopAvailable) {
            ShopItemStatus(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(top = 6.dp),
                product = uiState.product,
                selectedQuantity = uiState.selectedAmount,
                eventReceiver = eventReceiver
            )

            if (uiState.selectedAmount > 0) {
                Spacer(modifier = Modifier.weight(1f))

                InseatButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    text = stringResource(R.string.confirm_button),
                    onClick = { eventReceiver(ProductScreenEvent.OnConfirmClicked) },
                )
            }
        } else {
            InfoBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }
    }
}

@Composable
fun InfoBlock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEBEEF6))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.shop_has_not_opened_yet),
            style = N_16_24,
            color = Color(0xFF333333),
        )
    }
}

@Composable
private fun ShopItemStatus(
    product: Product,
    selectedQuantity: Int,
    eventReceiver: (ProductScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isMaxAmountReached = product.quantity == selectedQuantity.toLong()
    val isOutOfStock = product.quantity == 0L

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isOutOfStock && isMaxAmountReached) {
            LimitReachedLabel()
        } else {
            Spacer(modifier = Modifier.height(18.dp))
        }
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFFE2E2E2)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .padding(3.dp)
                    .let { modifier ->
                        if (selectedQuantity > 0) {
                            modifier.clickable { eventReceiver(ProductScreenEvent.OnRemoveItemClicked) }
                        } else {
                            modifier
                        }
                    },
                painter = painterResource(id = if (selectedQuantity > 0) R.drawable.ic_remove else R.drawable.ic_remove_dissabled),
                contentDescription = "Not selected"
            )

            if (isOutOfStock) {
                OutOfStockLabel()
            } else {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = selectedQuantity.toString(),
                    style = B_18,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center,
                )
            }

            Image(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp)
                    .padding(5.dp)
                    .let { modifier ->
                        if (!isMaxAmountReached) {
                            modifier.clickable { eventReceiver(ProductScreenEvent.OnAddItemClicked) }
                        } else {
                            modifier
                        }
                    },
                painter = painterResource(id = if (isMaxAmountReached) R.drawable.ic_plus_dissabled else R.drawable.ic_plus),
                contentDescription = "Not selected"
            )
        }
    }
}

@Composable
private fun OutOfStockLabel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
}

@Composable
private fun LimitReachedLabel(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(bottom = 4.dp)
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
}