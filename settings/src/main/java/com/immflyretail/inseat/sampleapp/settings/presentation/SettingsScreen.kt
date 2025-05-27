package com.immflyretail.inseat.sampleapp.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.basket_api.BasketScreenContract
import com.immflyretail.inseat.sampleapp.ui.BottomNavItem
import com.immflyretail.inseat.sampleapp.ui.BottomNavigationBar
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.Screen
import com.immflyretail.inseat.sampleapp.orders_api.OrdersScreenContract
import com.immflyretail.inseat.sampleapp.settings_api.SettingsScreenContract
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract

fun NavGraphBuilder.settingsScreen(navController: NavController) {
    composable<SettingsScreenContract.Route> {
        val viewModel: SettingsScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        SettingsScreen(
            uiState = uiState,
            onBottomNavSelected = { item ->
                when (item) {
                    BottomNavItem.Cart -> navController.navigate(BasketScreenContract.Route)
                    BottomNavItem.Shop -> navController.navigate(ShopScreenContract.Route)
                    BottomNavItem.MyOrders -> navController.navigate(OrdersScreenContract.Route)
                    BottomNavItem.Settings -> {}
                }
            },
            onAutoRefreshEnabled = {
                viewModel.obtainEvent(SettingsScreenEvent.OnAutoRefreshEnabled)
            },
            onManualRefreshEnabled = {
                viewModel.obtainEvent(SettingsScreenEvent.OnManualRefreshEnabled)
            }
        )
    }
}

@Composable
private fun SettingsScreen(
    uiState: SettingsScreenState,
    onBottomNavSelected: (BottomNavItem) -> Unit,
    onAutoRefreshEnabled: () -> Unit,
    onManualRefreshEnabled: () -> Unit,
    modifier: Modifier = Modifier
) {
    Screen(
        modifier = modifier,
        title = "Settings",
        bottomNavigation = { BottomNavigationBar { item -> onBottomNavSelected.invoke(item) } }
    ) {

        when (uiState) {
            is SettingsScreenState.DataLoaded -> ContentScreen(
                uiState = uiState,
                onAutoRefreshEnabled = { onAutoRefreshEnabled.invoke() },
                onManualRefreshEnabled = { onManualRefreshEnabled.invoke() },
            )

            SettingsScreenState.Loading -> Loading()
        }
    }
}

@Composable
private fun ContentScreen(
    uiState: SettingsScreenState.DataLoaded,
    onAutoRefreshEnabled: () -> Unit,
    onManualRefreshEnabled: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .height(64.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Data refresh type:",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFF333333),
                    )
                )

                val selectedBackground = Color(0xFFDD083A)
                val unselectedBackground = Color(0xFFF8F8F8)
                val selectedText = Color(0xFFFFFFFF)
                val unselectedText = Color(0xFF666666)

                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .width(120.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(unselectedBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(60.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .clickable { onAutoRefreshEnabled.invoke() }
                            .background(
                                if (uiState.isAutoRefreshEnabled) {
                                    selectedBackground
                                } else {
                                    unselectedBackground
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Auto",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight(400),
                                color = if (uiState.isAutoRefreshEnabled) {
                                    selectedText
                                } else {
                                    unselectedText
                                },
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(60.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .clickable { onManualRefreshEnabled.invoke() }
                            .background(
                                if (uiState.isAutoRefreshEnabled) {
                                    unselectedBackground
                                } else {
                                    selectedBackground
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Manual",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight(400),
                                color = if (uiState.isAutoRefreshEnabled) {
                                    unselectedText
                                } else {
                                    selectedText
                                },
                            )
                        )
                    }
                }
            }
        }
    }
}