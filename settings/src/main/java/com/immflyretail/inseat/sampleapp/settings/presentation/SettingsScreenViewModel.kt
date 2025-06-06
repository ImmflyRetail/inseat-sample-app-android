package com.immflyretail.inseat.sampleapp.settings.presentation

import androidx.lifecycle.ViewModel
import com.immflyretail.inseat.sampleapp.core.extension.runCoroutine
import com.immflyretail.inseat.sampleapp.settings.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsScreenState>(SettingsScreenState.Loading)
    val uiState: StateFlow<SettingsScreenState> get() = _uiState

    init {
        loadData()
    }

    fun obtainEvent(event: SettingsScreenEvent) {
        val state = _uiState.value

        if (state !is SettingsScreenState.DataLoaded) return

        when (event) {
            SettingsScreenEvent.OnAutoRefreshEnabled -> runCoroutine {
                repository.setAutoRefreshState(true)
                _uiState.value = state.copy(isAutoRefreshEnabled = true)
            }

            SettingsScreenEvent.OnManualRefreshEnabled -> runCoroutine {
                repository.setAutoRefreshState(false)
                _uiState.value = state.copy(isAutoRefreshEnabled = false)

            }
        }
    }

    private fun loadData() = runCoroutine {
        _uiState.value = SettingsScreenState.DataLoaded(repository.getAutoRefreshState())
    }
}