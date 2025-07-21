package com.immflyretail.inseat.sampleapp.shop.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenResultKey
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenResultKey
import com.immflyretail.inseat.sampleapp.product_api.ProductScreenResult
import com.immflyretail.inseat.sampleapp.shop.R
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopStatus
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.InseatButton
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14_22
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_16
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_18_26
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_22_30
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_8_13
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_10
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_12_20
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_16
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_16_24
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_18_26
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Menu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.immflyretail.inseat.sampleapp.ui.R as uiR

private val statusRawHeight = 44.dp

fun NavGraphBuilder.shopScreen(navController: NavController) {
    composable<ShopScreenContract.Route> {
        val viewModel: ShopScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ShopScreen(uiState, viewModel, navController)
    }
}

@Composable
fun ShopScreen(
    uiState: ShopScreenState,
    viewModel: ShopScreenViewModel,
    navController: NavController
) {
    val isNeedToShowSearchBar = uiState is ShopScreenState.DataLoaded && uiState.isSearchEnabled
    val searchFieldFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    Screen(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        title = stringResource(R.string.shop),
        isBackButtonEnabled = isNeedToShowSearchBar,
        customToolbar = if (isNeedToShowSearchBar) {
            {
                SearchToolbar(
                    query = (uiState as ShopScreenState.DataLoaded).searchQuery,
                    eventReceiver = viewModel::obtainEvent,
                    searchFieldFocusRequester = searchFieldFocusRequester,
                    modifier = Modifier
                        .padding(start = 40.dp, end = 16.dp)
                        .fillMaxWidth()
                )
            }
        } else null,
        toolbarItem = {
            when {
                uiState is ShopScreenState.DataLoaded -> {
                    ShopToolbar(uiState, eventReceiver = viewModel::obtainEvent)
                }

                uiState is ShopScreenState.SelectMenu -> {
                    Image(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.obtainEvent(ShopScreenEvent.OnSettingsClicked) }
                            .focusable(),
                        painter = painterResource(id = uiR.drawable.settings),
                        contentDescription = "Settings Icon"
                    )
                }
            }
        },
        onBackClicked = { viewModel.obtainEvent(ShopScreenEvent.OnBackClicked) }
    ) {
        when (uiState) {
            is ShopScreenState.Loading -> Loading()
            is ShopScreenState.DataLoaded -> {
                ContentScreen(uiState, viewModel::obtainEvent)
            }

            is ShopScreenState.Error -> ErrorScreen(uiState.message ?: "Error")
            is ShopScreenState.SelectMenu -> MenuSelector(
                menus = uiState.menus,
                onMenuSelected = { menuType ->
                    viewModel.obtainEvent(ShopScreenEvent.OnMenuSelected(menuType))
                }
            )
        }

        SingleEventEffect(viewModel.uiAction) { action ->
            when (action) {
                is ShopScreenActions.Navigate -> navController.execute(action.lambda)
                is ShopScreenActions.MoveFocusToSearch -> {
                    coroutineScope.launch {
                        delay(300) // Delay to ensure the search field is ready for focus
                        searchFieldFocusRequester.requestFocus()
                    }
                }
            }
        }

        BackHandler { viewModel.obtainEvent(ShopScreenEvent.OnBackClicked) }

        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle!!
        val productUpdatesObserver = savedStateHandle.getStateFlow(
            ProductScreenResultKey.REFRESHED_PRODUCT.name,
            ProductScreenResult()
        )
        val basketUpdatesObserver = savedStateHandle.getStateFlow(
            BasketScreenResultKey.PRODUCTS_IN_BASKET_REFRESHED.name,
            initialValue = false
        )

        SingleEventEffect(productUpdatesObserver) { result ->
            if (result.productId != -1) {
                viewModel.obtainEvent(
                    ShopScreenEvent.OnProductUpdated(result.productId, result.selectedAmount)
                )
                savedStateHandle.remove<ProductScreenResult>(ProductScreenResultKey.REFRESHED_PRODUCT.name)
            }
        }

        SingleEventEffect(basketUpdatesObserver) { result ->
            if (result == true) {
                viewModel.obtainEvent(ShopScreenEvent.ItemInBasketUpdated)
                savedStateHandle.remove<Boolean>(BasketScreenResultKey.PRODUCTS_IN_BASKET_REFRESHED.name)
            }
        }
    }
}

@Composable
private fun ShopToolbar(
    uiState: ShopScreenState.DataLoaded,
    eventReceiver: (ShopScreenEvent) -> Unit
) {
    Row(Modifier.padding(end = 16.dp)) {
        Image(
            modifier = Modifier
                .size(24.dp)
                .clickable { eventReceiver(ShopScreenEvent.OnSearchClicked) }
                .focusable(),
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search Icon"
        )

        if (uiState.shopStatus == ShopStatus.ORDER) {
            BasketIcon(
                modifier = Modifier.padding(start = 16.dp),
                itemsInBasket = uiState.itemsInBasket,
                eventReceiver = eventReceiver
            )
        }
    }
}

@Composable
private fun BasketIcon(
    itemsInBasket: Int,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.size(24.dp)
    ) {
        Image(
            modifier = Modifier
                .size(24.dp)
                .clickable { eventReceiver(ShopScreenEvent.OnCartClicked) }
                .focusable(),
            painter = painterResource(id = R.drawable.ic_basket),
            contentDescription = "Shopping Basket Icon"
        )
        if (itemsInBasket > 0) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(Color(0xFFD40E14), shape = CircleShape)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = itemsInBasket.toString(),
                    style = B_8_13,
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun OrdersInfo(
    ordersCount: Int,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8F8F8))
            .clickable { eventReceiver(ShopScreenEvent.OnOrdersClicked) },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            text = stringResource(R.string.shop_screen_my_orders_count, ordersCount),
            style = N_14,
            color = Color(0xFFDD083A)
        )

        Image(
            painterResource(R.drawable.ic_arrow_right_red),
            contentDescription = "Arrow",
            modifier = Modifier
                .padding(start = 8.dp)
                .size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentScreen(
    uiState: ShopScreenState.DataLoaded,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isPullToRefreshEnabled) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { eventReceiver(ShopScreenEvent.OnRefresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            MainData(uiState, eventReceiver, modifier)
        }
    } else {
        MainData(uiState, eventReceiver, modifier)
    }
}

@Composable
fun MainData(
    uiState: ShopScreenState.DataLoaded,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
        ) {
            if (uiState.ordersCount > 0) {
                OrdersInfo(
                    ordersCount = uiState.ordersCount,
                    eventReceiver = eventReceiver
                )
            }
            if (uiState.searchQuery.isEmpty()) {
                if (uiState.categories.isNotEmpty()) {
                    CategoryTabs(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFFF2F2F2)),
                        selectedTabIndex = uiState.selectedTabIndex,
                        categories = uiState.categories,
                        eventReceiver = eventReceiver
                    )
                }
            } else {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFFF2F2F2))
                        .padding(start = 16.dp, top = 20.dp),
                    text = stringResource(R.string.search_results, uiState.searchResult.size),
                    style = B_18_26,
                    color = Color(0xFF333333),
                )
            }

            val productListModifier =
                if (uiState.shopStatus != ShopStatus.ORDER) Modifier.padding(bottom = statusRawHeight) else Modifier

            when {
                uiState.isSearchEnabled && uiState.searchQuery.isNotEmpty() && uiState.searchResult.isNotEmpty() -> {
                    ProductsList(
                        items = uiState.searchResult,
                        shopStatus = uiState.shopStatus,
                        eventReceiver = eventReceiver,
                        modifier = productListModifier
                    )
                }

                uiState.isSearchEnabled && uiState.searchQuery.isNotEmpty() && uiState.searchResult.isEmpty() -> {
                    ItemNotFound()
                }

                else -> {
                    ProductsList(
                        items = uiState.items,
                        shopStatus = uiState.shopStatus,
                        eventReceiver = eventReceiver,
                        modifier = productListModifier
                    )
                }
            }
        }

        when (uiState.shopStatus) {
            ShopStatus.ORDER -> if (uiState.itemsInBasket > 0) {
                CartButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    onClick = { eventReceiver(ShopScreenEvent.OnCartClicked) }
                )
            }

            else -> StatusRow(
                shopStatus = uiState.shopStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusRawHeight)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(
    selectedTabIndex: Int,
    categories: List<Category>,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    SecondaryScrollableTabRow(
        modifier = modifier,
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    selectedTabIndex,
                    matchContentSize = false
                ),
                color = Color(0xFFDD083A)
            )
        },
        divider = {},
        edgePadding = 0.dp
    ) {
        categories.forEachIndexed { index, category ->
            val isTabSelected = index == selectedTabIndex
            Tab(
                selected = isTabSelected,
                onClick = { eventReceiver(ShopScreenEvent.OnCategorySelected(category, index)) },
                text = {
                    Text(
                        text = category.name,
                        style = if (isTabSelected) B_16 else N_16,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )
        }
    }
}

@Composable
private fun ProductsList(
    items: List<ShopItem>,
    shopStatus: ShopStatus,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (shopStatus != ShopStatus.ORDER) {
            item(span = { GridItemSpan(2) }) {
                InfoBlock()
            }
        }
        items(items) { item ->
            ListItem(item = item, eventReceiver = eventReceiver, shopStatus)
        }
    }
}

@Composable
fun ItemNotFound(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.no_result_found),
            style = B_22_30,
            color = Color(0xFF333333),
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.no_result_found_description),
            style = N_18_26,
            color = Color(0xFF666666),
        )
    }
}

@Composable
fun SearchToolbar(
    query: String,
    eventReceiver: (ShopScreenEvent) -> Unit,
    searchFieldFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            modifier = Modifier.focusRequester(searchFieldFocusRequester),
            textStyle = N_14,
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.ic_search),
                    contentDescription = "Search Icon"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = "Clear Search",
                        modifier = Modifier.clickable { eventReceiver(ShopScreenEvent.OnSearch("")) }
                    )
                }
            },
            placeholder = { Text(text = stringResource(R.string.search), style = N_14) },
            value = query,
            onValueChange = { eventReceiver(ShopScreenEvent.OnSearch(it)) },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors().copy(
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333),
                cursorColor = Color(0xFF333333),
                focusedPlaceholderColor = Color(0xFF666666),
                unfocusedPlaceholderColor = Color(0xFF666666),
                focusedContainerColor = Color(0xFFF8F8F8),
                unfocusedContainerColor = Color(0xFFF8F8F8),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLeadingIconColor = Color(0xFF666666),
                unfocusedLeadingIconColor = Color(0xFF666666)
            ),
        )
    }
}

@Composable
private fun ListItem(
    item: ShopItem,
    eventReceiver: (ShopScreenEvent) -> Unit,
    shopStatus: ShopStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(228.dp)
            .background(MaterialTheme.colorScheme.background)
            .clickable { eventReceiver(ShopScreenEvent.OnProductClicked(item.product.itemId)) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.Start,
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
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
                        painterResource(R.drawable.placeholder_image)
                    },
                    contentDescription = "Image"
                )

                if (shopStatus == ShopStatus.ORDER) {
                    ShopItemStatus(
                        item = item,
                        eventReceiver = eventReceiver
                    )
                }
            }

            val textColor = if (item.product.quantity > 0) {
                Color(0xFF333333)
            } else {
                Color(0xB3575555)
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                text = item.product.name,
                style = B_14_22,
                color = textColor,
            )

            val priceData = item.product.prices.first()
            val textStyle = N_12_20.copy(color = textColor)

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                text = priceData.price.toString() + " " + priceData.currency,
                style = textStyle,
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                text = stringResource(R.string.stock) + item.product.quantity.toString(),
                style = textStyle
            )
        }
    }
}

@Composable
private fun ShopItemStatus(
    item: ShopItem,
    eventReceiver: (ShopScreenEvent) -> Unit,
) {
    when {
        item.product.quantity == 0L -> {
            OutOfStockIcon(Modifier.padding(8.dp))
        }

        item.selectedQuantity.toLong() == item.product.quantity -> {
            LimitReachedIcon(
                item.selectedQuantity,
                item.product.itemId,
                eventReceiver,
                Modifier.padding(8.dp)
            )
        }

        item.selectedQuantity == 0 -> {
            NotSelectedIcon(item.product.itemId, eventReceiver, Modifier.padding(8.dp))
        }

        item.selectedQuantity > 0 && item.selectedQuantity < item.product.quantity -> {
            SelectedIcon(
                item.selectedQuantity,
                item.product.itemId,
                eventReceiver,
                Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun StatusRow(shopStatus: ShopStatus, modifier: Modifier = Modifier) {
    val rowBackground = if (shopStatus == ShopStatus.OPEN) {
        Color(0xFFFDF6E2)
    } else {
        Color(0xFFFDE7E8)
    }

    val statusText = when (shopStatus) {
        ShopStatus.OPEN -> "Open to browse"
        ShopStatus.CLOSED -> "Closed"
        ShopStatus.DEFAULT -> "Offline"
        else -> ""
    }

    val rowTextColor = Color(0xFFD40E14)

    Row(
        modifier = modifier
            .background(rowBackground)
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Shop Status: ", color = rowTextColor)
        Text(text = statusText, color = rowTextColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OutOfStockIcon(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
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
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(12.dp),
                painter = painterResource(id = R.drawable.ic_plus_dissabled),
                contentDescription = "Not selected"
            )
        }
    }
}

@Composable
fun NotSelectedIcon(
    itemId: Int,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0xFFE2E2E2))
            .clickable { eventReceiver(ShopScreenEvent.OnAddItemClicked(itemId)) },
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(12.dp),
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = "Not selected",
        )
    }
}

@Composable
fun SelectedIcon(
    selectedQuantity: Int,
    itemId: Int,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
                .clickable { eventReceiver(ShopScreenEvent.OnRemoveItemClicked(itemId)) },
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
                .clickable { eventReceiver(ShopScreenEvent.OnAddItemClicked(itemId)) },
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = "Not selected"
        )
    }
}

@Composable
fun LimitReachedIcon(
    selectedQuantity: Int,
    itemId: Int,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
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

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
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
                    .clickable { eventReceiver(ShopScreenEvent.OnRemoveItemClicked(itemId)) },
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
    }
}

@Composable
fun MenuSelector(
    menus: List<Menu>,
    onMenuSelected: (menu: Menu) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(R.string.menu_selection),
            style = B_22_30,
            color = Color(0xFF333333),
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.choose_which_menu_you_want_to_view_based_on_your_preferences),
            style = N_18_26,
            color = Color(0xFF333333),
        )

        LazyColumn(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = menus, itemContent = { menu ->
                InseatButton(
                    text = menu.displayName.first().text,
                    onClick = { onMenuSelected.invoke(menu) }
                )
            })
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
            text = stringResource(R.string.the_store_is_not_open_yet),
            style = N_16_24,
            color = Color(0xFF333333),
        )
    }
}

@Composable
fun CartButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    InseatButton(
        modifier = modifier,
        text = stringResource(R.string.shop_cart_button),
        icon = {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                painter = painterResource(id = R.drawable.ic_cart_for_button),
                contentDescription = "Cart Icon"
            )
        },
        onClick = onClick
    )

}
