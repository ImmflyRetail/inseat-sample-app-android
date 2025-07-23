package com.immflyretail.inseat.sampleapp.basket.presentation.checkout

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.basket.R
import com.immflyretail.inseat.sampleapp.basket.presentation.basket.model.BasketItem
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.InseatButton
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

@OptIn(ExperimentalMaterial3Api::class)
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
//        val sheetState = rememberModalBottomSheetState()

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
                        )
                    } else {
                        CollapsedSummary(
                            items = uiState.items,
                            total = uiState.total,
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
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

@Composable
private fun ExpandedSummary(
    items: List<BasketItem>,
    total: BigDecimal,
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
            text = "Summary",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF333333),
            )
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
                text = "Total",
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF333333),
                )
            )

            Text(
                text = "${total.toPlainString()} $currency",
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
                text = "${items.size} items for",
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF333333),
                )
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "${total.toPlainString()} $currency",
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
            text = item.product.name,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF333333),
                textAlign = TextAlign.Start,
            )
        )

        val price = item.product.prices.first()
        Text(
            text = "${price.price.toPlainString()} ${price.currency}",
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
            text = "Enter your details",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF333333),
            )
        )

        OutlinedTextField(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            value = enteredSeatNumber,
            onValueChange = { onSeatNumberEntered.invoke(it) },
            label = {
                Text(
                    text = "What’s your seat number?",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFF666666),
                    )
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
            text = "You’ll pay your order to a crew member when they deliver it to you.",
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF333333),
            )
        )
    }
}
