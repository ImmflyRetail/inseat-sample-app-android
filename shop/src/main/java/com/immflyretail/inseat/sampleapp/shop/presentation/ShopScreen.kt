package com.immflyretail.inseat.sampleapp.shop.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.ui.BottomNavItem
import com.immflyretail.inseat.sampleapp.ui.BottomNavigationBar
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.ImmseatButton
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.shop.R
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sdk.api.models.Category
import com.immflyretail.inseat.sdk.api.models.Menu
import com.immflyretail.inseat.sampleapp.settings_api.SettingsScreenContract
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopItem
import com.immflyretail.inseat.sampleapp.shop.presentation.model.ShopStatus
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect

fun NavGraphBuilder.shopScreen(navController: NavController) {
    composable<ShopScreenContract.Route> {
        val viewModel: ShopScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ShopScreen(
            uiState,
            viewModel,
            onBottomNavSelected = { item ->
                when (item) {
                    BottomNavItem.Cart -> navController.navigate(BasketScreenContract.Route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }

                    BottomNavItem.Shop -> {}
                    BottomNavItem.MyOrders -> navController.navigate(OrdersScreenContract.Route)
                    BottomNavItem.Settings -> navController.navigate(SettingsScreenContract.Route)
                }
            },
        )
    }
}

@Composable
fun ShopScreen(
    uiState: ShopScreenState,
    viewModel: ShopScreenViewModel,
    onBottomNavSelected: (BottomNavItem) -> Unit,
) {

    Screen(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        title = "Shop",
        bottomNavigation = { BottomNavigationBar { onBottomNavSelected.invoke(it) } },
        toolbarItem = {
            if (uiState is ShopScreenState.DataLoaded) {
                Image(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp)
                        .clickable { viewModel.obtainEvent(ShopScreenEvent.ClickOnCategories) }
                        .focusable(),
                    painter = painterResource(id = R.drawable.ic_filters),
                    contentDescription = "Collections"
                )
            }
        }
    ) {
        when (uiState) {
            is ShopScreenState.Loading -> Loading()
            is ShopScreenState.DataLoaded -> ContentScreen(uiState, viewModel)

            is ShopScreenState.Error -> ErrorScreen(uiState.message ?: "Error")
            is ShopScreenState.SelectMenu -> MenuSelector(
                menus = uiState.menus,
                onMenuSelected = { menuType ->
                    viewModel.obtainEvent(ShopScreenEvent.OnMenuSelected(menuType))
                }
            )
        }

        val context = LocalContext.current
        SingleEventEffect(viewModel.uiAction) { action ->
            when (action) {
                is ShopScreenActions.Message -> Toast
                    .makeText(context, action.text, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentScreen(
    uiState: ShopScreenState.DataLoaded,
    viewModel: ShopScreenViewModel,
    modifier: Modifier = Modifier
) {
    if (uiState.isPullToRefreshEnabled) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.obtainEvent(ShopScreenEvent.OnRefresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            MainData(uiState, viewModel, modifier)
        }
    } else {
        MainData(uiState, viewModel, modifier)
    }
}

@Composable
fun MainData(
    uiState: ShopScreenState.DataLoaded,
    viewModel: ShopScreenViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        StatusRow(uiState.shopStatus)

        if (uiState.shopStatus == ShopStatus.DEFAULT) {
            InfoBlock(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(uiState.items) { item ->
                ListItem(item = item, viewModel = viewModel)
            }
        }

        if (uiState.categories != null) {
            CategoriesDialog(categories = uiState.categories, onDismissRequest = {
                viewModel.obtainEvent(
                    ShopScreenEvent.CloseCategories
                )
            })
        }
    }
}

@Composable
fun CategoriesDialog(
    categories: List<Category>,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismissRequest.invoke() },
        content = {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
            ) {

                LazyColumn(
                    modifier = Modifier
                        .background(Color(0xFFFFFFFF))
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(24.dp),
                ) {
                    item {
                        Text(
                            modifier = Modifier.padding(bottom = 16.dp),
                            text = "Categories",
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontSize = 22.sp,
                                lineHeight = 30.sp,
                                fontWeight = FontWeight(600),
                                color = Color(0xFF333333),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                    itemsIndexed(items = categories) { index, category ->
                        Text(
                            text = category.name,
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 66.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFF333333),
                                textAlign = TextAlign.Center,
                            )
                        )

                        if (index != categories.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 8.dp,
                                        bottom = 8.dp
                                    ),
                                color = Color(0xFFE2E2E2),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ListItem(
    item: ShopItem,
    viewModel: ShopScreenViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(228.dp)
            .background(MaterialTheme.colorScheme.background),
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

                when {
                    item.product.quantity == 0L -> {
                        OutOfStockIcon(Modifier.padding(8.dp))
                    }

                    item.selectedQuantity.toLong() == item.product.quantity -> {
                        LimitReachedIcon(
                            item.selectedQuantity,
                            item.product.itemId,
                            viewModel,
                            Modifier.padding(8.dp)
                        )
                    }

                    item.selectedQuantity == 0 -> {
                        NotSelectedIcon(item.product.itemId, viewModel, Modifier.padding(8.dp))
                    }

                    item.selectedQuantity > 0 && item.selectedQuantity < item.product.quantity -> {
                        SelectedIcon(
                            item.selectedQuantity,
                            item.product.itemId,
                            viewModel,
                            Modifier.padding(8.dp)
                        )
                    }

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
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(600),
                    color = textColor,
                )
            )
            val priceData = item.product.prices.first()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                text = priceData.price.toString() + " " + priceData.currency,
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(400),
                    color = textColor,
                ),
            )
        }
    }
}

@Composable
fun StatusRow(shopStatus: ShopStatus, modifier: Modifier = Modifier) {
    val rowBackground = when (shopStatus) {
        ShopStatus.OPEN -> Color(0xFFE1F4E6)
        ShopStatus.CLOSED -> Color(0xFFDAA7AD)
        ShopStatus.ORDER -> Color(0xFFEADAAE)
        ShopStatus.DEFAULT -> Color(0xFF7E7E7E)
    }
    val rowTextColor = when (shopStatus) {
        ShopStatus.OPEN -> Color(0xFF109C42)
        ShopStatus.CLOSED -> Color(0xFF4F0C14)
        ShopStatus.ORDER -> Color(0xFF362B08)
        ShopStatus.DEFAULT -> Color(0xFF000000)
    }
    Row(
        modifier = modifier
            .background(rowBackground)
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Shop Status: ", color = rowTextColor)
        Text(text = shopStatus.name, color = rowTextColor, fontWeight = FontWeight.Bold)
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
                text = "Out of stock",
                style = TextStyle(
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFD40E14),
                    textAlign = TextAlign.Center,
                )
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
    viewModel: ShopScreenViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0xFFE2E2E2))
            .clickable { viewModel.obtainEvent(ShopScreenEvent.OnAddItemClicked(itemId)) },
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
    viewModel: ShopScreenViewModel,
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
                .clickable {
                    viewModel.obtainEvent(ShopScreenEvent.OnRemoveItemClicked(itemId))
                },
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
                .clickable {
                    viewModel.obtainEvent(ShopScreenEvent.OnAddItemClicked(itemId))
                },
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = "Not selected"
        )
    }
}

@Composable
fun LimitReachedIcon(
    selectedQuantity: Int,
    itemId: Int,
    viewModel: ShopScreenViewModel,
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
                    .clickable {
                        viewModel.obtainEvent(ShopScreenEvent.OnRemoveItemClicked(itemId))
                    },
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
            text = "Menu selection",
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF333333),
            )
        )

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Choose which menu you want to view based on your preferences.",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF333333),
            )
        )

        LazyColumn(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = menus, itemContent = { menu ->
                ImmseatButton(
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
            text = "The store is not open yet! Feel free to explore the available products, but keep in mind that prices and stock may change when the store officially opens.",
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF333333),
            )
        )
    }
}
