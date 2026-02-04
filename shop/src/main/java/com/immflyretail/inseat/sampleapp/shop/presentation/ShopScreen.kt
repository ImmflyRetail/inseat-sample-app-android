package com.immflyretail.inseat.sampleapp.shop.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.shop.R
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopStatus
import com.immflyretail.inseat.sampleapp.shop.presentation.model.TabItem
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract
import com.immflyretail.inseat.sampleapp.ui.AppButton
import com.immflyretail.inseat.sampleapp.ui.AppIconButton
import com.immflyretail.inseat.sampleapp.ui.ButtonStyle
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_14_22
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_16
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_16_24
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_18_26
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_22_30
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_8_13
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_10
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_12_20
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_14
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_14_22
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_16
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_16_24
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_18_26
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.AppScaffold
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sampleapp.ui.utils.IconWrapper
import com.immflyretail.inseat.sdk.api.models.Menu
import com.immflyretail.inseat.sdk.api.models.Promotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.immflyretail.inseat.sampleapp.core.resources.R as CoreR

private val statusRawHeight = 44.dp
private const val currency = "EUR"

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
    val isNeedToShowSearchBar =
        uiState is ShopScreenState.DataLoaded && uiState.isSearchEnabled
    val searchFieldFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    AppScaffold(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        title = stringResource(R.string.shop),
        isBackButtonEnabled = isNeedToShowSearchBar,
        topBarSearch = if (isNeedToShowSearchBar) {
            {
                SearchTopBar(
                    query = (uiState as ShopScreenState.DataLoaded).searchQuery,
                    eventReceiver = viewModel::obtainEvent,
                    searchFieldFocusRequester = searchFieldFocusRequester,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
        } else null,
        topBarActions = {
            when {
                uiState is ShopScreenState.DataLoaded -> {
                    ShopActions(uiState, eventReceiver = viewModel::obtainEvent)
                }

                uiState is ShopScreenState.SelectMenu -> {
                    AppIconButton(
                        icon = IconWrapper.Vector(Icons.Default.Settings),
                        onClick = { viewModel.obtainEvent(ShopScreenEvent.OnSettingsClicked) },
                        contentDescriptionId = R.string.shop_settings_icon_content_description
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

            is ShopScreenState.Error -> ErrorScreen(
                uiState.message ?: stringResource(id = R.string.shop_error_message)
            )

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
    }
}

@Composable
private fun ShopActions(
    uiState: ShopScreenState.DataLoaded,
    eventReceiver: (ShopScreenEvent) -> Unit
) {
    if (uiState.tabs[uiState.selectedTabIndex] !is TabItem.PromotionTab && !uiState.isSearchEnabled) {
        AppIconButton(
            icon = IconWrapper.Vector(Icons.Outlined.Search),
            onClick = { eventReceiver(ShopScreenEvent.OnSearchClicked) },
            contentDescriptionId = R.string.shop_search_icon_content_description
        )

        if (uiState.shopStatus == ShopStatus.ORDER) {
            BasketIcon(
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
    Box(modifier = modifier) {
        AppIconButton(
            icon = IconWrapper.Vector(Icons.Outlined.ShoppingCart),
            onClick = { eventReceiver(ShopScreenEvent.OnCartClicked) },
            contentDescriptionId = R.string.shop_basket_icon_content_description
        )
        if (itemsInBasket > 0) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(Color(0xFFD40E14), shape = CircleShape)
                    .align(Alignment.BottomCenter),
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        AppButton(
            style = ButtonStyle.Flat,
            text = stringResource(R.string.shop_screen_my_orders_count, ordersCount),
            onClick = { eventReceiver(ShopScreenEvent.OnOrdersClicked) },
            trailingIcon = IconWrapper.Vector(Icons.Outlined.ChevronRight),
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8F8F8)),
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

                if (uiState.tabs.isNotEmpty()) {
                    ShopTabs(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFFF2F2F2)),
                        selectedTabIndex = uiState.selectedTabIndex,
                        tabs = uiState.tabs,
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

            val selectedTab = uiState.tabs[uiState.selectedTabIndex]
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

                selectedTab is TabItem.PromotionTab -> PromotionsList(
                    items = selectedTab.promotions,
                    currency = uiState.currency,
                    shopStatus = uiState.shopStatus,
                    eventReceiver = eventReceiver,
                )

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
private fun ShopTabs(
    selectedTabIndex: Int,
    tabs: List<TabItem>,
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
        tabs.forEachIndexed { index, tab ->
            val isTabSelected = index == selectedTabIndex
            Tab(
                selected = isTabSelected,
                onClick = { eventReceiver(ShopScreenEvent.OnTabSelected(tab, index)) },
                text = {
                    Text(
                        text = tab.getTabName(),
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
private fun PromotionsList(
    items: List<Promotion>,
    currency: String,
    shopStatus: ShopStatus,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 54.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (shopStatus != ShopStatus.ORDER) {
            item { InfoBlock() }
        }
        items(items) { item ->
            PromotionItem(item = item, currency = currency, eventReceiver = eventReceiver)
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
fun SearchTopBar(
    query: String,
    eventReceiver: (ShopScreenEvent) -> Unit,
    searchFieldFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            modifier = Modifier
                .focusRequester(searchFieldFocusRequester)
                .fillMaxWidth()
                .padding(end = 8.dp),
            textStyle = N_14,
            leadingIcon = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = stringResource(id = R.string.shop_search_icon_content_description)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(id = R.string.shop_clear_search_content_description),
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
            .clip(RoundedCornerShape(16.dp))
            .clickable { eventReceiver(ShopScreenEvent.OnProductClicked(item.product.itemId)) }
            .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
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
                        painterResource(CoreR.drawable.im_food_placeholder)
                    },
                    contentDescription = stringResource(id = CoreR.string.not_selected_content_description)
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

            val priceData =
                item.product.prices.find { it.currency == currency } ?: item.product.prices.first()
            val textStyle = N_12_20.copy(color = textColor)

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                text = priceData.amount.toString() + " " + priceData.currency,
                style = textStyle,
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                text = stringResource(id = R.string.shop_stock_label, item.product.quantity),
                style = textStyle
            )
        }
    }
}

@Composable
private fun PromotionItem(
    item: Promotion,
    currency: String,
    eventReceiver: (ShopScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { eventReceiver(ShopScreenEvent.OnPromotionClicked(item.promotionId)) },
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = item.name,
                        style = B_16_24,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF333333)
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = item.discountText,
                        style = N_14_22,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF666666)
                    )

                    val discount = item.discounts.firstOrNull { it.currency == currency }
                    var textColor = Color(0xFF109C42)
                    val savings = when (item.benefitType) {
                        Promotion.BenefitType.DISCOUNT -> when (item.discountType) {
                            Promotion.DiscountType.PERCENTAGE -> stringResource(
                                id = R.string.shop_promotion_discount_percentage_off,
                                item.discountPercent.roundToInt()
                            )

                            Promotion.DiscountType.AMOUNT -> stringResource(
                                id = R.string.shop_promotion_discount_amount_off,
                                discount?.discount.toString(),
                                currency
                            )

                            Promotion.DiscountType.FIXED_PRICE -> {
                                textColor = Color(0xFF333333)
                                stringResource(
                                    id = R.string.shop_promotion_fixed_price,
                                    discount?.discount.toString(),
                                    currency
                                )
                            }

                            Promotion.DiscountType.COUPON -> stringResource(id = R.string.shop_get_a_voucher)
                        }

                        Promotion.BenefitType.COUPON -> stringResource(id = R.string.shop_get_a_voucher)
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = savings,
                        style = B_18_26,
                        color = textColor
                    )
                }
                val bitmap = try {
                    val decodedBytes =
                        Base64.decode(
                            item.base64image?.encodeToByteArray() ?: byteArrayOf(),
                            Base64.DEFAULT
                        )
                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        .asImageBitmap()
                } catch (e: Exception) {
                    null
                }

                if (bitmap != null) {
                    Image(
                        modifier = Modifier
                            .width(106.dp)
                            .height(90.dp),
                        bitmap = bitmap,
                        contentDescription = stringResource(id = R.string.shop_promotion_item_image_content_description)
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .width(106.dp)
                            .height(90.dp),
                        painter = painterResource(CoreR.drawable.im_food_placeholder),
                        contentDescription = stringResource(id = R.string.shop_promotion_item_image_content_description)
                    )
                }
            }

            Box(
                modifier = modifier
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color(0xFFE2E2E2), shape = CircleShape)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(id = CoreR.string.not_selected_content_description),
                )
            }
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
        ShopStatus.OPEN -> stringResource(id = R.string.shop_status_open_to_browse)
        ShopStatus.CLOSED -> stringResource(id = R.string.shop_status_closed)
        ShopStatus.DEFAULT -> stringResource(id = R.string.shop_status_offline)
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
        Text(stringResource(id = R.string.shop_status_label) + " ", color = rowTextColor)
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
                .wrapContentWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                text = stringResource(CoreR.string.out_of_stock),
                style = N_10,
                color = Color(0xFFD40E14),
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                tint = Color(0xB3575555),
                contentDescription = stringResource(id = CoreR.string.not_selected_content_description)
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
    AppIconButton(
        icon = IconWrapper.Vector(Icons.Outlined.Add),
        onClick = { eventReceiver(ShopScreenEvent.OnAddItemClicked(itemId)) },
        contentDescriptionId = CoreR.string.add_item_content_description,
        containerColor = Color(0xFFE2E2E2),
        modifier = modifier.size(32.dp)
    )
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
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFFE2E2E2)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppIconButton(
            icon = IconWrapper.Vector(Icons.Outlined.Remove),
            onClick = { eventReceiver(ShopScreenEvent.OnRemoveItemClicked(itemId)) },
            contentDescriptionId = CoreR.string.remove_item_content_description,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = selectedQuantity.toString(),
            style = B_14,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
        )

        AppIconButton(
            icon = IconWrapper.Vector(Icons.Outlined.Add),
            onClick = { eventReceiver(ShopScreenEvent.OnAddItemClicked(itemId)) },
            contentDescriptionId = CoreR.string.add_item_content_description,
            modifier = Modifier.size(32.dp)
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
                .wrapContentWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8F8F8)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                text = stringResource(CoreR.string.limit_reached),
                style = N_10,
                color = Color(0xFFD40E14),
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFFE2E2E2)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppIconButton(
                icon = IconWrapper.Vector(Icons.Outlined.Remove),
                onClick = { eventReceiver(ShopScreenEvent.OnRemoveItemClicked(itemId)) },
                contentDescriptionId = CoreR.string.remove_item_content_description,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = selectedQuantity.toString(),
                style = B_14,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
            )
            AppIconButton(
                icon = IconWrapper.Vector(Icons.Outlined.Add),
                onClick = { },
                isEnabled = false,
                contentDescriptionId = CoreR.string.not_selected_content_description,
                modifier = Modifier.size(32.dp)
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
                AppButton(
                    text = menu.displayName.first().text,
                    onClick = { onMenuSelected.invoke(menu) },
                    modifier = Modifier.fillMaxWidth(),
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
    AppButton(
        text = stringResource(R.string.shop_cart_button),
        onClick = onClick,
        leadingIcon = IconWrapper.Vector(Icons.Outlined.ShoppingCart),
        modifier = modifier.fillMaxWidth(),
    )
}
