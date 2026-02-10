package com.immflyretail.inseat.sampleapp.orders.presentation.status

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.orders.R
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_14
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_14_22
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_16_24
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_18
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_24_32
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_10
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_12
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_14
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_14_22
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_16_24
import com.immflyretail.inseat.sampleapp.theme.green
import com.immflyretail.inseat.sampleapp.ui.AppButton
import com.immflyretail.inseat.sampleapp.ui.AppScaffold
import com.immflyretail.inseat.sampleapp.ui.ButtonStyle
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sampleapp.ui.AppAnimatedBottomSheet
import com.immflyretail.inseat.sdk.api.models.AppliedPromotion
import com.immflyretail.inseat.sdk.api.models.Order
import com.immflyretail.inseat.sdk.api.models.OrderItem
import com.immflyretail.inseat.sdk.api.models.OrderStatusEnum
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale
import com.immflyretail.inseat.sampleapp.core.resources.R as CoreR

private val dateFormat = SimpleDateFormat("dd/MM/yy, HH:mm", Locale.getDefault())

fun NavGraphBuilder.ordersStatusScreen(navController: NavController) {
    composable<OrdersScreenContract.OrderStatusRoute> {
        val viewModel: OrderStatusScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        OrderStatusScreen(uiState, viewModel, navController)
    }
}

@Composable
fun OrderStatusScreen(
    uiState: OrderStatusScreenState,
    viewModel: OrderStatusScreenViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    AppScaffold(
        modifier = modifier,
        title = stringResource(R.string.order_status_title),
        onBackClicked = { viewModel.obtainEvent(OrderStatusScreenEvent.OnBackClicked) },
    ) {

        Crossfade(
            targetState = uiState,
            label = "StateTransition"
        ) { state ->
            when (state) {
                is OrderStatusScreenState.Data -> ContentScreen(
                    uiState = state,
                    eventReceiver = viewModel::obtainEvent,
                )
                is OrderStatusScreenState.Error -> ErrorScreen(state.message)
                OrderStatusScreenState.Loading -> Loading()
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
                viewModel.obtainEvent(OrderStatusScreenEvent.OnConfirmOrderCancellationClicked)
            },
            secondaryButtonText = stringResource(CoreR.string.keep_order),
            onSecondaryButtonClick = { showBottomSheet = false },
        )
    }

    SingleEventEffect(viewModel.uiAction) { action ->
        when (action) {
            is OrderStatusScreenActions.Navigate -> navController.execute(action.lambda)
            OrderStatusScreenActions.ShowBottomSheet -> {
                showBottomSheet = true
            }
        }
    }
}

@Composable
private fun ContentScreen(
    uiState: OrderStatusScreenState.Data,
    eventReceiver: (OrderStatusScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OrderStatus(uiState.order.status)
        OrderDetails(order = uiState.order)
        if (uiState.order.status == OrderStatusEnum.PLACED || uiState.order.status == OrderStatusEnum.RECEIVED) {
            OrderCancellation(onCancelOrderClicked = { eventReceiver(OrderStatusScreenEvent.OnCancelOrderClicked) })
        }
    }
}

@Composable
fun OrderStatus(status: OrderStatusEnum, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)

    ) {
        Text(
            modifier = modifier.padding(16.dp),
            text = stringResource(R.string.order_status_your_order_s_in),
            style = B_24_32
        )

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrderStatusItem(
                name = stringResource(R.string.order_status_order_placed),
                isActive = true,
                modifier = Modifier.weight(1f),
            )
            OrderStatusItem(
                name = stringResource(R.string.order_status_in_preparation),
                isActive = status == OrderStatusEnum.PREPARING || status == OrderStatusEnum.COMPLETED,
                modifier = Modifier.weight(1f)
            )
            OrderStatusItem(
                name = stringResource(R.string.order_status_delivered),
                isActive = status == OrderStatusEnum.COMPLETED,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.order_status_your_order_will_be_ready_soon),
            style = N_10
        )
    }
}

@Composable
fun OrderStatusItem(name: String, isActive: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { 1f },
            color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
        Text(
            modifier = Modifier.padding(top = 7.dp),
            text = name,
            style = N_14
        )
    }
}

@Composable
private fun OrderDetails(
    order: Order,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
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
                text = dateFormat.format(order.createdAt),
                style = N_12,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = stringResource(R.string.details),
            style = B_16_24,
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

        val savings = order.appliedPromotions.sumOf {
            (it.benefitType as? AppliedPromotion.BenefitType.Discount)?.totalSavings?.amount
                ?: BigDecimal.ZERO
        }
        val total = order.totalPrice - savings

        if (savings > BigDecimal.ZERO) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(CoreR.string.subtotal),
                    style = B_14,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "${order.totalPrice} ${order.currency}",
                    style = B_14,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Right,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(CoreR.string.savings),
                    style = B_14,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = "-${savings} ${order.currency}",
                    style = B_14,
                    color = green,
                    textAlign = TextAlign.Right,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(CoreR.string.total),
                style = B_18,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = "${total} ${order.currency}",
                style = B_18,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Right,
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
fun OrderCancellation(modifier: Modifier = Modifier, onCancelOrderClicked: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)

    ) {
        Text(
            text = stringResource(R.string.order_status_cansellation_title),
            style = N_16_24
        )

        AppButton(
            style = ButtonStyle.Flat,
            text = stringResource(CoreR.string.cancel_order),
            onClick = onCancelOrderClicked,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

