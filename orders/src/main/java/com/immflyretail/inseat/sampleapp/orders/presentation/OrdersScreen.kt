package com.immflyretail.inseat.sampleapp.orders.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.core.constants.DATE_FORMAT
import com.immflyretail.inseat.sampleapp.orders.R
import com.immflyretail.inseat.sampleapp.ui.BottomNavItem
import com.immflyretail.inseat.sampleapp.ui.BottomNavigationBar
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sdk.api.models.Order
import com.immflyretail.inseat.sdk.api.models.OrderItem
import com.immflyretail.inseat.sampleapp.settings_api.SettingsScreenContract
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract
import java.text.SimpleDateFormat
import java.util.Locale

fun NavGraphBuilder.ordersScreen(navController: NavController) {
    composable<OrdersScreenContract.Route> {
        val viewModel: OrdersScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        OrdersScreen(
            uiState = uiState,
            onBottomNavSelected = {
                when (it) {
                    BottomNavItem.Cart -> navController.navigate(BasketScreenContract.Route)
                    BottomNavItem.Shop -> navController.navigate(ShopScreenContract.Route)
                    BottomNavItem.MyOrders -> navController.navigate(OrdersScreenContract.Route)
                    BottomNavItem.Settings -> navController.navigate(SettingsScreenContract.Route)
                }
            },
            onCancelOrderClicked = { orderId ->
                viewModel.obtainEvent(
                    OrdersScreenEvent.OnCancelOrderClicked(orderId)
                )
            },
        )
    }
}

@Composable
private fun OrdersScreen(
    uiState: OrdersScreenState,
    onBottomNavSelected: (BottomNavItem) -> Unit,
    onCancelOrderClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Screen(
        modifier = modifier,
        title = "My orders",
        bottomNavigation = { BottomNavigationBar { item -> onBottomNavSelected.invoke(item) } }
    ) {
        when (uiState) {
            is OrdersScreenState.Data -> ContentScreen(
                uiState = uiState,
                onCancelOrderClicked = { onCancelOrderClicked.invoke(it) },
            )

            is OrdersScreenState.Error -> ErrorScreen(uiState.message)
            OrdersScreenState.Loading -> Loading()
        }
    }
}

@Composable
private fun ContentScreen(
    uiState: OrdersScreenState.Data,
    onCancelOrderClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(items = uiState.items) { order ->
            val isExpanded = remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (isExpanded.value) {
                    ExpandedOrder(
                        order = order,
                        onDetailsClicked = { isExpanded.value = false },
                        onCancelOrderClicked = { onCancelOrderClicked.invoke(it) },
                    )
                } else {
                    CollapsedOrder(
                        order = order,
                        onDetailsClicked = { isExpanded.value = true },
                        onCancelOrderClicked = { onCancelOrderClicked.invoke(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedOrder(
    order: Order,
    modifier: Modifier = Modifier,
    onDetailsClicked: () -> Unit,
    onCancelOrderClicked: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(order.createdAt) ,
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF666666),
                )
            )

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE2E2E2))
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = order.status.name,
                    style = TextStyle(
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight(600),
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                    )
                )
            }
            Image(
                painterResource(R.drawable.remove),
                contentDescription = "Remove",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .clickable { onCancelOrderClicked.invoke(order.id) }
            )
        }

        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = "Details",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF333333),
            )
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Order ID",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF666666),
            )
        )

        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = order.id,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF333333),
            )
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Seat number",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF666666),
            )
        )

        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = order.customerSeatNumber,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF333333),
            )
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = Color(0xFFE2E2E2),
            thickness = 1.dp
        )

        order.items.forEach {
            ProductItem(
                item = it,
                currency = order.currency,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = Color(0xFFE2E2E2),
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth()
                .clickable { onDetailsClicked.invoke() },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Total",
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                )
            )

            Text(
                text = "${order.totalPrice} ${order.currency}",
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Right,
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clickable { onDetailsClicked.invoke() },
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
        ) {

            Text(
                text = "Hide order details",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFDD083A),
                    textDecoration = TextDecoration.Underline,
                )
            )

            Image(
                modifier = Modifier.padding(start = 4.dp),
                painter = painterResource(id = R.drawable.up_arrow),
                contentDescription = "image description",
            )
        }
    }
}

@Composable
private fun CollapsedOrder(
    order: Order,
    modifier: Modifier = Modifier,
    onDetailsClicked: () -> Unit,
    onCancelOrderClicked: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(order.createdAt) ,
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF666666),
                )
            )

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE2E2E2))
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = order.status.name,
                    style = TextStyle(
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight(600),
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                    )
                )
            }
            Image(
                painterResource(R.drawable.remove),
                contentDescription = "Remove",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .clickable { onCancelOrderClicked.invoke(order.id) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
        ) {

            Text(
                text = "${order.items.size} items for",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF333333),
                )
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${order.totalPrice} ${order.currency}",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Right,
                )
            )
        }

        HorizontalDivider(
            color = Color(0xFFE2E2E2),
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clickable { onDetailsClicked.invoke() },
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
        ) {

            Text(
                text = "View order details",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFDD083A),
                    textDecoration = TextDecoration.Underline,
                )
            )

            Image(
                modifier = Modifier.padding(start = 4.dp),
                painter = painterResource(id = R.drawable.down_arrow),
                contentDescription = "image description",
            )
        }
    }
}

@Composable
private fun ProductItem(
    item: OrderItem,
    currency: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${item.quantity}x",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
            )
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
                .fillMaxWidth(),
            text = item.name,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF333333),
                textAlign = TextAlign.Start,
            )
        )

        val price = item.price
        Text(
            text = "$price $currency",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF333333),
                textAlign = TextAlign.End,
            )
        )
    }
}
