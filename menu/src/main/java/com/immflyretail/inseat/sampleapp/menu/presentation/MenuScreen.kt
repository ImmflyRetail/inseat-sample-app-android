package com.immflyretail.inseat.sampleapp.menu.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.menu.R
import com.immflyretail.inseat.sampleapp.shop_api.MenuScreenContract
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_18_26
import com.immflyretail.inseat.sampleapp.ui.AppButton
import com.immflyretail.inseat.sampleapp.ui.AppIconButton
import com.immflyretail.inseat.sampleapp.ui.AppScaffold
import com.immflyretail.inseat.sampleapp.ui.ErrorScreen
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect
import com.immflyretail.inseat.sampleapp.ui.utils.IconWrapper
import com.immflyretail.inseat.sdk.api.models.Menu

fun NavGraphBuilder.menuSelectorScreen(navController: NavController) {
    composable<MenuScreenContract.Route> {
        val viewModel: MenuScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        MenuScreen(uiState, viewModel, navController)
    }
}

@Composable
fun MenuScreen(
    uiState: MenuScreenState,
    viewModel: MenuScreenViewModel,
    navController: NavController
) {
    SingleEventEffect(viewModel.uiAction) { action ->
        when (action) {
            is MenuScreenActions.Navigate -> navController.execute(action.lambda)
        }
    }

    BackHandler { viewModel.obtainEvent(MenuScreenEvent.OnBackClicked) }

    AppScaffold(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        title = stringResource(R.string.menu_selection),
        isBackButtonEnabled = false,
        topBarActions = {
            AppIconButton(
                icon = IconWrapper.Vector(Icons.Default.Settings),
                onClick = { viewModel.obtainEvent(MenuScreenEvent.OnSettingsClicked) },
                contentDescriptionId = R.string.settings_icon_content_description
            )
        },
    ) {
        when (uiState) {
            is MenuScreenState.Loading -> Loading()

            is MenuScreenState.Error -> ErrorScreen(
                uiState.message ?: stringResource(R.string.error_message)
            )

            is MenuScreenState.DataLoaded -> MenuContent(
                uiState = uiState,
                viewModel::obtainEvent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuContent(
    uiState: MenuScreenState.DataLoaded,
    eventReceiver: (MenuScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isPullToRefreshEnabled) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { eventReceiver(MenuScreenEvent.OnRefresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            MenuSelector(modifier, uiState.menus, eventReceiver)
        }
    } else {
        MenuSelector(modifier, uiState.menus, eventReceiver)
    }
}

@Composable
private fun MenuSelector(
    modifier: Modifier,
    menus: List<Menu>,
    eventReceiver: (MenuScreenEvent) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(R.string.choose_which_menu_you_want_to_view_based_on_your_preferences),
                style = N_18_26,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        items(items = menus) { menu ->
            AppButton(
                text = menu.displayName.firstOrNull()?.text ?: "Menu",
                onClick = { eventReceiver(MenuScreenEvent.OnMenuSelected(menu)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}