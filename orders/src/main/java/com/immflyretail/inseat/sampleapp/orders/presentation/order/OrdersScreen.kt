package com.immflyretail.inseat.sampleapp.orders.presentation.order

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
import androidx.compose.ui.res.stringResource
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
import com.immflyretail.inseat.sampleapp.core.constants.DATE_FORMAT
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.orders.R
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_10
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14_22
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_16_24
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_18
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_18_26
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_12
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_14_22
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_16_24
import com.immflyretail.inseat.sdk.api.models.Order
import com.immflyretail.inseat.sdk.api.models.OrderItem
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sdk.api.models.OrderStatusEnum
import java.text.SimpleDateFormat
import java.util.Locale

fun NavGraphBuilder.ordersScreen(navController: NavController) {
    composable<OrdersScreenContract.OrdersListRoute> {
        val viewModel: OrdersScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        OrdersScreen(uiState, viewModel, navController)
    }
}

@Composable
private fun OrdersScreen(
    uiState: OrdersScreenState,
    viewModel: OrdersScreenViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Screen(
        modifier = modifier,
        title = stringResource(R.string.my_orders),
        onBackClicked = { viewModel.obtainEvent(OrdersScreenEvent.OnBackClicked) },
    ) {
        when (uiState) {
            is OrdersScreenState.Data -> ContentScreen(
                uiState = uiState,
                eventReceiver = viewModel::obtainEvent
            )

            is OrdersScreenState.Error -> ErrorScreen(uiState.message)
            OrdersScreenState.Loading -> Loading()
        }
    }

    SingleEventEffect(viewModel.uiAction) { action ->
        when (action) {
            is OrderScreenAction.Navigate -> navController.execute(action.lambda)
        }
    }
}

@Composable
private fun ContentScreen(
    uiState: OrdersScreenState.Data,
    eventReceiver: (OrdersScreenEvent) -> Unit,
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
                if (order.status == OrderStatusEnum.PLACED || order.status == OrderStatusEnum.RECEIVED) {
                    JustPlacedOrder(order = order, eventReceiver = eventReceiver)
                } else {
                    if (isExpanded.value) {
                        ExpandedOrder(
                            order = order,
                            onDetailsClicked = { isExpanded.value = false },
                        )
                    } else {
                        CollapsedOrder(
                            order = order,
                            onDetailsClicked = { isExpanded.value = true },
                        )
                    }
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
                text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(order.createdAt),
                style = N_12,
                color = Color(0xFF666666),
            )

            OrderStatus(status = order.status)
        }

        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = stringResource(R.string.details),
            style = B_18_26,
            color = Color(0xFF333333),
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.order_id),
            style = N_14,
            color = Color(0xFF666666),
        )

        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = order.id,
            style = B_14_22,
            color = Color(0xFF333333),
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.seat_number),
            style = N_14,
            color = Color(0xFF666666),
        )

        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = order.customerSeatNumber,
            style = B_14_22,
            color = Color(0xFF333333),
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
                text = stringResource(R.string.total),
                style = B_18,
                color = Color(0xFF333333),
            )

            Text(
                text = "${order.totalPrice} ${order.currency}",
                style = B_18,
                color = Color(0xFF333333),
                textAlign = TextAlign.Right,
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
                text = stringResource(R.string.hide_order_details),
                style = N_14,
                color = Color(0xFFDD083A),
                textDecoration = TextDecoration.Underline,
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
                text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(order.createdAt),
                style = N_12,
                color = Color(0xFF666666),
            )

            OrderStatus(status = order.status)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
        ) {

            Text(
                text = stringResource(R.string.items_for, order.items.size),
                style = N_16_24,
                color = Color(0xFF333333),
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${order.totalPrice} ${order.currency}",
                style = B_16_24,
                color = Color(0xFF333333),
                textAlign = TextAlign.Right,
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
            val text = if (order.status == OrderStatusEnum.COMPLETED) {
                stringResource(R.string.view_order_details)
            } else {
                stringResource(R.string.view_order_status)
            }

            Text(
                text = text,
                style = N_14,
                color = Color(0xFFDD083A),
                textDecoration = TextDecoration.Underline,
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
private fun JustPlacedOrder(
    order: Order,
    modifier: Modifier = Modifier,
    eventReceiver: (OrdersScreenEvent) -> Unit,
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
                text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(order.createdAt),
                style = N_12,
                color = Color(0xFF666666),
            )

            OrderStatus(status = order.status)
            Image(
                painterResource(R.drawable.remove),
                contentDescription = "Remove",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .clickable { eventReceiver(OrdersScreenEvent.OnCancelOrderClicked(order.id)) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
        ) {

            Text(
                text = stringResource(R.string.items_for,order.items.size),
                style = N_16_24,
                color = Color(0xFF333333),
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${order.totalPrice} ${order.currency}",
                style = B_16_24,
                color = Color(0xFF333333),
                textAlign = TextAlign.Right,
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
                .clickable { eventReceiver(OrdersScreenEvent.OnDetailsClicked(order.id)) },
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
        ) {

            Text(
                text = stringResource(R.string.view_order_details),
                style = N_14,
                color = Color(0xFFDD083A),
                textDecoration = TextDecoration.Underline,
            )

            Image(
                modifier = Modifier.padding(start = 4.dp),
                painter = painterResource(id = R.drawable.ic_arrow_right_red),
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
            style = B_14,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
                .fillMaxWidth(),
            text = item.name,
            style = N_14_22,
            color = Color(0xFF333333),
            textAlign = TextAlign.Start,
        )

        val price = item.price
        Text(
            text = "$price $currency",
            style = B_14,
            color = Color(0xFF333333),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun OrderStatus(status: OrderStatusEnum, modifier: Modifier = Modifier) {
    val text: String
    val textColor: Color
    val labelColor: Color

    when (status) {
        OrderStatusEnum.PLACED, OrderStatusEnum.RECEIVED -> {
            text = stringResource(R.string.order_placed)
            textColor = Color(0xFF666666)
            labelColor = Color(0xFFE2E2E2)
        }
        OrderStatusEnum.COMPLETED -> {
            text = stringResource(R.string.delivered)
            textColor = Color(0xFF109C42)
            labelColor = Color(0xFFE1F4E6)
        }

        OrderStatusEnum.PREPARING -> {
            text = stringResource(R.string.in_preparation)
            textColor = Color(0xFFF39300)
            labelColor = Color(0xFFFDF6E2)
        }

        OrderStatusEnum.CANCELLED_BY_CREW, OrderStatusEnum.CANCELLED_BY_TIMEOUT, OrderStatusEnum.CANCELLED_BY_PASSENGER -> {
            text = stringResource(R.string.cancelled)
            textColor = Color(0xFFD40E14)
            labelColor = Color(0xFFFDE7E8)
        }

        OrderStatusEnum.REFUNDED -> {
            text = stringResource(R.string.refunded)
            textColor = Color(0xFFD40E14)
            labelColor = Color(0xFFFDE7E8)
        }
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(labelColor)
    ) {
        Text(
            modifier = modifier.padding(8.dp),
            text = text,
            style = B_10,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}
