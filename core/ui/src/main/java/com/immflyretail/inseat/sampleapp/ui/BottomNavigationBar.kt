package com.immflyretail.inseat.sampleapp.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


enum class BottomNavItem(@DrawableRes val icon: Int, val label: String) {
    Shop(R.drawable.shop, "Shop"),
    Cart(R.drawable.cart, "Cart"),
    MyOrders(R.drawable.my_orders, "My Orders"),
    Settings(R.drawable.settings, "Settings")
}

private var selectedItem = BottomNavItem.Shop

@Composable
fun BottomNavigationBar(onItemSelected: (BottomNavItem) -> Unit) {

    BottomAppBar (
        contentPadding = PaddingValues(horizontal = 10.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomNavItem.entries.forEach { item ->
                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = Color(0xFFDD083A),
                        selectedIconColor = Color(0xFFDD083A),
                        unselectedTextColor = Color(0xFF666666),
                        unselectedIconColor = Color(0xFF666666),
                        indicatorColor = Color.Transparent
                    ),
                    label = {
                        Text (
                            item.label,
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight(600),
                                textAlign = TextAlign.Center,
                            ),
                            color = if (selectedItem == item) Color(0xFFDD083A) else Color(0xFF666666)
                        )
                    },
                    alwaysShowLabel = true,
                    selected = selectedItem == item,
                    onClick = {
                        selectedItem = item
                        onItemSelected.invoke(item)
                    },
                    icon = {
                        Icon(painter = painterResource(item.icon), contentDescription = item.label)
                    }
                )
            }
        }
    }
}