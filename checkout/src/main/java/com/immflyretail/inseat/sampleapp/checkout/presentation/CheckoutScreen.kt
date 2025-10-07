package com.immflyretail.inseat.sampleapp.checkout.presentation

import  androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.checkout.R
import com.immflyretail.inseat.sampleapp.checkout.presentation.models.BasketItem
import com.immflyretail.inseat.sampleapp.checkout_api.CheckoutScreenContract
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.InseatButton
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_16_24
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_18
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_18_26
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_14_22
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_16_24
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import java.math.BigDecimal

fun NavGraphBuilder.checkoutScreen(navController: NavController) {
    composable<CheckoutScreenContract.Route> {
        val viewModel: CheckoutScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        CheckoutScreen(
            uiState = uiState,
            navController = navController,
            viewModel = viewModel
        )
    }
}

@Composable
private fun CheckoutScreen(
    uiState: CheckoutScreenState,
    navController: NavController,
    viewModel: CheckoutScreenViewModel,
    modifier: Modifier = Modifier
) {
    Screen(
        modifier = modifier,
        title = "Checkout",
        onBackClicked = { viewModel.obtainEvent(CheckoutScreenEvent.OnBackClicked) },
    ) {
        var showBottomSheet by remember { mutableStateOf(false) }
        var isOrderSuccess by remember { mutableStateOf(false) }

        when (uiState) {
            is CheckoutScreenState.Data -> ContentScreen(
                uiState = uiState,
                onMakeOrderClicked = { viewModel.obtainEvent(CheckoutScreenEvent.OnMakeOrderClicked) },
                onDetailsClicked = { viewModel.obtainEvent(CheckoutScreenEvent.OnDetailsClicked) },
                onSeatNumberEntered = {
                    viewModel.obtainEvent(
                        CheckoutScreenEvent.OnSeatNumberEntered(
                            it
                        )
                    )
                }
            )

            is CheckoutScreenState.Error -> ErrorScreen(uiState.message)
            CheckoutScreenState.Loading -> Loading()
        }

        SingleEventEffect(viewModel.uiAction) { action ->
            when (action) {
                is CheckoutScreenActions.Navigate -> navController.execute(action.lambda)
                is CheckoutScreenActions.ShowDialog -> {
                    isOrderSuccess = action.isOrderSuccess
                    showBottomSheet = true
                }
            }
        }

        if (showBottomSheet) {
            OrderResultDialog(
                isOrderSuccess = isOrderSuccess,
                onClickClose = { viewModel.obtainEvent(CheckoutScreenEvent.OnClickKeepShopping) },
                onClickOrderStatus = { viewModel.obtainEvent(CheckoutScreenEvent.OnClickOrderStatus) },
                onDismissRequest = { showBottomSheet = false },
            )
        }
    }
}

@Composable
private fun ContentScreen(
    uiState: CheckoutScreenState.Data,
    modifier: Modifier = Modifier,
    onMakeOrderClicked: () -> Unit = {},
    onDetailsClicked: () -> Unit = {},
    onSeatNumberEntered: (String) -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {

        val isSeatNumberValid = uiState.seatNumber.matches(Regex("^[0-9]{1,2}[A-Za-z]$"))
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    if (uiState.isExpanded) {
                        ExpandedSummary(
                            items = uiState.items,
                            total = uiState.total,
                            currency = uiState.currency,
                            onDetailsClicked = onDetailsClicked,
                            savings = uiState.savings
                        )
                    } else {
                        CollapsedSummary(
                            items = uiState.items,
                            total = uiState.total - uiState.savings,
                            currency = uiState.currency,
                            onDetailsClicked = onDetailsClicked,
                        )
                    }
                }
            }

            item {
                EnterDetailsBlock(
                    enteredSeatNumber = uiState.seatNumber,
                    isSeatNumberValid = isSeatNumberValid,
                    onSeatNumberEntered = onSeatNumberEntered
                )
            }

            item { InfoBlock() }
        }

        InseatButton(
            text = stringResource(R.string.order_now),
            onClick = { onMakeOrderClicked.invoke() },
            isEnabled = isSeatNumberValid,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun ExpandedSummary(
    items: List<BasketItem>,
    total: BigDecimal,
    savings: BigDecimal,
    currency: String,
    modifier: Modifier = Modifier,
    onDetailsClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.summary),
            style = B_18_26,
            color = Color(0xFF333333),
        )

        items.forEach {
            ProductItem(
                item = it,
                modifier = Modifier.padding(top = 16.dp)
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
                text = stringResource(R.string.subtotal),
                style = B_18,
                color = Color(0xFF333333),
            )

            Text(
                text = "${total.toPlainString()} $currency",
                style = B_18,
                color = Color(0xFF333333),
                textAlign = TextAlign.Right,
            )
        }
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth()
                .clickable { onDetailsClicked.invoke() },
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.savings),
                style = B_18,
                color = Color(0xFF333333),
            )

            Text(
                text = "-${savings.toPlainString()} $currency",
                style = B_18,
                color = Color(0xFF109C42),
                textAlign = TextAlign.Right,
            )
        }

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
                text = "${(total - savings).toPlainString()} $currency",
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
private fun CollapsedSummary(
    items: List<BasketItem>,
    total: BigDecimal,
    currency: String,
    modifier: Modifier = Modifier,
    onDetailsClicked: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.aligned(Alignment.CenterHorizontally),
        ) {

            Text(
                text = stringResource(R.string.items_for, items.size),
                style = N_16_24,
                color = Color(0xFF333333),
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${total.toPlainString()} $currency",
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

            Text(
                text = stringResource(R.string.view_order_details),
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
private fun ProductItem(
    item: BasketItem,
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
            text = item.product.name,
            style = N_14_22,
            color = Color(0xFF333333),
            textAlign = TextAlign.Start,
        )

        val price = item.product.prices.first()
        Text(
            text = "${price.amount.toPlainString()} ${price.currency}",
            style = B_14,
            color = Color(0xFF333333),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun EnterDetailsBlock(
    enteredSeatNumber: String,
    isSeatNumberValid: Boolean,
    modifier: Modifier = Modifier,
    onSeatNumberEntered: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.enter_your_details),
            style = B_18_26,
            color = Color(0xFF333333),
        )

        OutlinedTextField(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            value = enteredSeatNumber,
            onValueChange = { onSeatNumberEntered.invoke(it) },
            label = {
                Text(
                    text = stringResource(R.string.what_s_your_seat_number),
                    style = N_14_22,
                    color = Color(0xFF666666),
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFF2F2F2),
                focusedBorderColor = Color(0xFFF2F2F2),
                cursorColor = Color(0xFF333333),
            ),
            trailingIcon = {
                if (isSeatNumberValid) {
                    Image(
                        painterResource(R.drawable.completed),
                        contentDescription = "Completed",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            )
        )
    }
}

@Composable
fun ForcePromotionBlock(
    promotionId: String,
    modifier: Modifier = Modifier,
    onPromotionEntered: (String) -> Unit,
    onApplyPromotionClicked: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter promotion ID for force usage",
            style = B_18_26,
            color = Color(0xFF333333),
        )

        OutlinedTextField(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            value = promotionId,
            onValueChange = { onPromotionEntered.invoke(it) },
            label = {
                Text(
                    text = "Promotion ID",
                    style = N_14_22,
                    color = Color(0xFF666666),
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFF2F2F2),
                focusedBorderColor = Color(0xFFF2F2F2),
                cursorColor = Color(0xFF333333),
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            )
        )

        InseatButton(
            text = "Apply promo",
            onClick = { onApplyPromotionClicked.invoke(promotionId) },
            isEnabled = promotionId.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

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
            text = stringResource(R.string.you_ll_pay_your_order_to_a_crew_member_when_they_deliver_it_to_you),
            style = N_16_24,
            color = Color(0xFF333333),
        )
    }
}
