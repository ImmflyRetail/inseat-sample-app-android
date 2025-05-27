package com.immflyretail.inseat.sampleapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.immflyretail.inseat.sampleapp.basket.presentation.basket.basketScreen
import com.immflyretail.inseat.sampleapp.basket.presentation.checkout.checkoutScreen
import com.immflyretail.inseat.sampleapp.orders.presentation.ordersScreen
import com.immflyretail.inseat.sampleapp.settings.presentation.settingsScreen
import com.immflyretail.inseat.sampleapp.shop.presentation.shopScreen
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract

@Composable
fun NavigationHost() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ShopScreenContract.Route) {
        shopScreen(navController)
        basketScreen(navController)
        checkoutScreen(navController)
        ordersScreen(navController)
        settingsScreen(navController)
    }
}