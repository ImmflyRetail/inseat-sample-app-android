package com.immflyretail.inseat.sampleapp.menu.presentation

import androidx.lifecycle.ViewModel
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.navigation.popBackStackOrFinish
import com.immflyretail.inseat.sampleapp.settings_api.SettingsScreenContract
import com.immflyretail.inseat.sampleapp.menu.data.MenuRepository
import com.immflyretail.inseat.sampleapp.shop_api.ShopScreenContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class MenuScreenViewModel @Inject constructor(
    private val repository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MenuScreenState>(MenuScreenState.Loading)
    val uiState: StateFlow<MenuScreenState> get() = _uiState

    private val _uiAction = Channel<MenuScreenActions>()
    val uiAction: Flow<MenuScreenActions> get() = _uiAction.receiveAsFlow()

    init {
        loadMenus()
    }

    fun obtainEvent(event: MenuScreenEvent) {
        when (event) {
            is MenuScreenEvent.OnRefresh -> {
                _uiState.value = (_uiState.value as MenuScreenState.DataLoaded).copy(isRefreshing = true)
                loadMenus()
            }

            is MenuScreenEvent.OnMenuSelected -> runCoroutine {
                repository.selectMenu(event.menu)
                _uiAction.send(MenuScreenActions.Navigate { navigate(ShopScreenContract.Route) })
            }

            MenuScreenEvent.OnBackClicked -> runCoroutine {
                _uiAction.send(MenuScreenActions.Navigate { popBackStackOrFinish() })
            }

            MenuScreenEvent.OnSettingsClicked -> runCoroutine {
                _uiAction.send(MenuScreenActions.Navigate { navigate(SettingsScreenContract.Route) })
            }
        }
    }

    private fun loadMenus() = runCoroutine {
        try {
            val menus = repository.getAvailableMenus()
            repository.isAutoupdateEnabled()
                .onEach { isAutoupdate ->
                    _uiState.value = MenuScreenState.DataLoaded(
                        menus = menus,
                        isPullToRefreshEnabled = !isAutoupdate,
                        isRefreshing = false
                    )
                }
                .collect()
        } catch (e: Exception) {
            _uiState.value = MenuScreenState.Error(e.message)
        }
    }
}