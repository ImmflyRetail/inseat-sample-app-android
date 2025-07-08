package com.immflyretail.inseat.sampleapp.basket.presentation.basket

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.basket.R
import com.immflyretail.inseat.sampleapp.basket.presentation.basket.model.BasketItem
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.InseatButton
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14_22
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_16
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_18_26
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_22_30
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_10
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_12_20
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import java.math.BigDecimal

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
    Screen(
        modifier = modifier,
        title = "Shopping cart",
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
    }
}

@Composable
private fun ContentScreen(
    uiState: BasketScreenState.DataLoaded,
    viewModel: BasketScreenViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                text = stringResource(R.string.summary),
                style = B_22_30,
                color = Color(0xFF333333)
            )

            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = uiState.items, itemContent = { item ->
                    ListItem(
                        item = item,
                        onAddClicked = {viewModel.obtainEvent(BasketScreenEvent.OnAddItemClicked(it))},
                        onRemoveClicked = {viewModel.obtainEvent(BasketScreenEvent.OnRemoveItemClicked(it))}
                    )
                })
                item {
                    SummaryBlock(uiState.total, uiState.currency)
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .wrapContentWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .clickable { viewModel.obtainEvent(BasketScreenEvent.OnAddMoreClicked) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(5.dp),
                    painter = painterResource(id = R.drawable.ic_plus_red),
                    contentDescription = "Plus icon"
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = "Add more",
                    style = B_16,
                    color = Color(0xFFDD083A)
                )
            }
            InseatButton(
                text = "Checkout",
                onClick = { viewModel.obtainEvent(BasketScreenEvent.OnCheckoutClicked) },
                isEnabled = uiState.items.isNotEmpty()
            )

        }
    }
}

@Composable
private fun ListItem(
    item: BasketItem,
    onAddClicked: (Int) -> Unit,
    onRemoveClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(106.dp)
            .background(MaterialTheme.colorScheme.background),
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
                        painterResource(R.drawable.no_image)
                    },
                    contentDescription = "Image"
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .wrapContentWidth(),
                        text = item.product.name,
                        style = B_14_22,
                        color = Color(0xFF333333),
                    )

                    when {
                        item.quantity.toLong() == item.product.quantity -> LimitReachedIcon(
                            selectedQuantity = item.quantity,
                            itemId = item.product.itemId,
                            onRemoveClicked
                        )

                        item.quantity > 0 -> NormalIcon(
                            selectedQuantity = item.quantity,
                            itemId = item.product.itemId,
                            onAddClicked,
                            onRemoveClicked
                        )
                    }
                }

                val priceData = item.product.prices.first()
                Text(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(bottom = 30.dp)
                        .align(Alignment.CenterEnd),
                    text = priceData.price.toString() + " " + priceData.currency,
                    style = N_12_20,
                    color = Color(0xFF333333),
                )
            }
        }
    }
}

@Composable
fun NormalIcon(
    selectedQuantity: Int,
    itemId: Int,
    onAddClicked: (Int) -> Unit,
    onRemoveClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(24.dp)
            .width(106.dp)
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
                .clickable { onRemoveClicked.invoke(itemId) },
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
                .clickable { onAddClicked.invoke(itemId) },
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = "Not selected"
        )
    }
}

@Composable
fun LimitReachedIcon(
    selectedQuantity: Int,
    itemId: Int,
    onRemoveClicked: (Int) -> Unit,
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
                    .clickable { onRemoveClicked.invoke(itemId) },
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
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
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
}

@Composable
fun SummaryBlock(
    total: BigDecimal,
    currency: String,
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
            color = Color(0xFFE2E2E2),
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
                text = stringResource(R.string.total),
                style = B_18_26,
                color = Color(0xFF333333),
            )
            Text(
                text = total.toPlainString() + " " + currency,
                style = B_18_26,
                color = Color(0xFF333333),
                textAlign = TextAlign.Right,
            )
        }
        Spacer(Modifier.height(80.dp))
    }
}
