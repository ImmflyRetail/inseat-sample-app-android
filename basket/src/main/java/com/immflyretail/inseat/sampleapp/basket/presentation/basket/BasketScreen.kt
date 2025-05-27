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
import com.immflyretail.inseat.sampleapp.basket.presentation.checkout.CheckoutScreenContract
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.ui.BottomNavItem
import com.immflyretail.inseat.sampleapp.ui.BottomNavigationBar
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.ImmseatButton
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sampleapp.settings_api.SettingsScreenContract
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract
import java.math.BigDecimal

fun NavGraphBuilder.basketScreen(navController: NavController) {
    composable<BasketScreenContract.Route> {
        val viewModel: BasketScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        BasketScreen(
            uiState = uiState,
            onBottomNavSelected = { item ->
                when (item) {
                    BottomNavItem.Cart -> {}
                    BottomNavItem.Shop -> navController.navigate(ShopScreenContract.Route)
                    BottomNavItem.MyOrders -> navController.navigate(OrdersScreenContract.Route)
                    BottomNavItem.Settings -> navController.navigate(SettingsScreenContract.Route)
                }
            },
            onMakeOrderClicked = {
                navController.navigate(CheckoutScreenContract.Route)
            },
            onAddClicked = { itemId ->
                viewModel.obtainEvent(BasketScreenEvent.OnAddItemClicked(itemId))
            },
            onRemoveClicked = { itemId ->
                viewModel.obtainEvent(BasketScreenEvent.OnRemoveItemClicked(itemId))
            }
        )
    }
}

@Composable
private fun BasketScreen(
    uiState: BasketScreenState,
    onBottomNavSelected: (BottomNavItem) -> Unit,
    onMakeOrderClicked: () -> Unit,
    onAddClicked: (Int) -> Unit,
    onRemoveClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Screen(
        modifier = modifier,
        title = "Shopping cart",
        bottomNavigation = { BottomNavigationBar { item -> onBottomNavSelected.invoke(item) } }
    ) {

        when (uiState) {
            is BasketScreenState.DataLoaded -> ContentScreen(
                uiState = uiState,
                onMakeOrderClicked = { onMakeOrderClicked.invoke() },
                onRemoveClicked = { onRemoveClicked.invoke(it) },
                onAddClicked = { onAddClicked.invoke(it) }
            )

            is BasketScreenState.Error -> ErrorScreen(uiState.message)
            BasketScreenState.Loading -> Loading()
        }
    }
}

@Composable
private fun ContentScreen(
    uiState: BasketScreenState.DataLoaded,
    onMakeOrderClicked: () -> Unit = {},
    onAddClicked: (Int) -> Unit = {},
    onRemoveClicked: (Int) -> Unit = {},
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
                text = "Summary",
                style = TextStyle(
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                )
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
                        onAddClicked = onAddClicked,
                        onRemoveClicked = onRemoveClicked
                    )
                })
                item {
                    SummaryBlock(uiState.total, uiState.currency)
                }
            }
        }

        ImmseatButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            text = "Checkout",
            onClick = { onMakeOrderClicked.invoke() },
            isEnabled = uiState.items.isNotEmpty()
        )
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
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(600),
                            color = Color(0xFF333333),
                        )
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
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFF333333),
                    ),
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
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
            )
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
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center,
                )
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
                text = "Limit reached",
                style = TextStyle(
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFD40E14),
                    textAlign = TextAlign.Center,
                )
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
                text = "Total",
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                )
            )
            Text(
                text = total.toPlainString() + " " + currency,
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Right,
                )
            )
        }
        Spacer(Modifier.height(80.dp))
    }
}
