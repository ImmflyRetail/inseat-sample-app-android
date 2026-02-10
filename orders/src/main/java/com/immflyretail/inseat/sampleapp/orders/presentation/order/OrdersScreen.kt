package com.immflyretail.inseat.sampleapp.orders.presentation.order

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.core.constants.DATE_FORMAT
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.orders.R
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_10
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_14
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_14_22
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_16_24
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_18
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_18_26
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_12
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_14
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_14_22
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_16_24
import com.immflyretail.inseat.sampleapp.theme.green
import com.immflyretail.inseat.sampleapp.theme.orange
import com.immflyretail.inseat.sampleapp.theme.red
import com.immflyretail.inseat.sampleapp.theme.superLightGreen
import com.immflyretail.inseat.sampleapp.theme.superLightOrange
import com.immflyretail.inseat.sampleapp.theme.superLightRed
import com.immflyretail.inseat.sampleapp.ui.AppButton
import com.immflyretail.inseat.sampleapp.ui.AppIconButton
import com.immflyretail.inseat.sampleapp.ui.AppScaffold
import com.immflyretail.inseat.sampleapp.ui.ButtonStyle
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sampleapp.ui.AppAnimatedBottomSheet
import com.immflyretail.inseat.sampleapp.ui.utils.IconWrapper
import com.immflyretail.inseat.sdk.api.models.Order
import com.immflyretail.inseat.sdk.api.models.OrderItem
import com.immflyretail.inseat.sdk.api.models.OrderStatusEnum
import java.text.SimpleDateFormat
import java.util.Locale
import com.immflyretail.inseat.sampleapp.core.resources.R as CoreR

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
    var showBottomSheet by remember { mutableStateOf(false) }

    AppScaffold(
        modifier = modifier,
        title = stringResource(R.string.my_orders),
        onBackClicked = { viewModel.obtainEvent(OrdersScreenEvent.OnBackClicked) },
    ) {

        Crossfade(
            targetState = uiState,
            label = "StateTransition"
        ) { state ->
            when (state) {
                is OrdersScreenState.Data -> ContentScreen(
                    uiState = state,
                    eventReceiver = viewModel::obtainEvent
                )

                is OrdersScreenState.Error -> ErrorScreen(state.message)
                OrdersScreenState.Loading -> Loading()
            }
        }

        AppAnimatedBottomSheet(
            isVisible = showBottomSheet,
            onDismissClicked = { showBottomSheet = false },
            title = stringResource(CoreR.string.cancel_order_title),
            description = stringResource(CoreR.string.cancel_order_description),
            primaryButtonText = stringResource(CoreR.string.cancel_order),
            onPrimaryButtonClick = {
                showBottomSheet = false
                viewModel.obtainEvent(OrdersScreenEvent.OnConfirmOrderCancellationClicked)
            },
            secondaryButtonText = stringResource(CoreR.string.keep_order),
            onSecondaryButtonClick = { showBottomSheet = false },
        )
    }

    SingleEventEffect(viewModel.uiAction) { action ->
        when (action) {
            is OrdersScreenActions.Navigate -> navController.execute(action.lambda)
            OrdersScreenActions.ShowBottomSheet -> {
                showBottomSheet = true
            }
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
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = uiState.items) { order ->
            val isExpanded = remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .animateItem(),
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
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(order.createdAt),
                style = N_12,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OrderStatus(status = order.status)
        }

        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = stringResource(R.string.details),
            style = B_18_26,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.order_id),
            style = N_14,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = order.id,
            style = B_14_22,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.seat_number),
            style = N_14,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = order.customerSeatNumber,
            style = B_14_22,
            color = MaterialTheme.colorScheme.onBackground,
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
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
            color = MaterialTheme.colorScheme.tertiaryContainer,
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(CoreR.string.total),
                style = B_18,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = "${order.totalPrice} ${order.currency}",
                style = B_18,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Right,
            )
        }

        AppButton(
            style = ButtonStyle.Link,
            text = stringResource(CoreR.string.hide_order_details),
            onClick = onDetailsClicked,
            trailingIcon = IconWrapper.Vector(Icons.Outlined.KeyboardArrowUp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
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
                .padding(top = 12.dp)
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(order.createdAt),
                style = N_12,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                text = stringResource(CoreR.string.items_for, order.items.size),
                style = N_16_24,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${order.totalPrice} ${order.currency}",
                style = B_16_24,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Right,
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            thickness = 1.dp
        )

        AppButton(
            style = ButtonStyle.Link,
            text = if (order.status == OrderStatusEnum.COMPLETED) {
                stringResource(CoreR.string.view_order_details)
            } else {
                stringResource(R.string.view_order_status)
            },
            onClick = onDetailsClicked,
            trailingIcon = IconWrapper.Vector(Icons.Outlined.KeyboardArrowDown),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
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
                .padding(top = 12.dp)
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(order.createdAt),
                style = N_12,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OrderStatus(status = order.status)

            AppIconButton(
                icon = IconWrapper.Vector(Icons.Outlined.Delete),
                onClick = { eventReceiver(OrdersScreenEvent.OnCancelOrderClicked(order.id)) },
                contentColor = MaterialTheme.colorScheme.primary,
                contentDescriptionId = R.string.orders_remove_content_description
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
        ) {

            Text(
                text = stringResource(CoreR.string.items_for, order.items.size),
                style = N_16_24,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${order.totalPrice} ${order.currency}",
                style = B_16_24,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Right,
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            thickness = 1.dp
        )

        AppButton(
            style = ButtonStyle.Link,
            text = stringResource(CoreR.string.view_order_details),
            onClick = { eventReceiver(OrdersScreenEvent.OnDetailsClicked(order.id)) },
            trailingIcon = IconWrapper.Vector(Icons.Outlined.ChevronRight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
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
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
                .fillMaxWidth(),
            text = item.name,
            style = N_14_22,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start,
        )

        val price = item.price
        Text(
            text = "$price $currency",
            style = B_14,
            color = MaterialTheme.colorScheme.onBackground,
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
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
            labelColor = MaterialTheme.colorScheme.tertiaryContainer
        }

        OrderStatusEnum.COMPLETED -> {
            text = stringResource(R.string.delivered)
            textColor = green
            labelColor = superLightGreen
        }

        OrderStatusEnum.PREPARING -> {
            text = stringResource(R.string.in_preparation)
            textColor = orange
            labelColor = superLightOrange
        }

        OrderStatusEnum.CANCELLED_BY_CREW, OrderStatusEnum.CANCELLED_BY_TIMEOUT, OrderStatusEnum.CANCELLED_BY_PASSENGER -> {
            text = stringResource(R.string.cancelled)
            textColor = red
            labelColor = superLightRed
        }

        OrderStatusEnum.REFUNDED -> {
            text = stringResource(R.string.refunded)
            textColor = red
            labelColor = superLightRed
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
