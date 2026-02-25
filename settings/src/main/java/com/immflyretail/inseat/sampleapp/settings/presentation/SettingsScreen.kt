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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.immflyretail.inseat.sampleapp.core.extension.execute
import com.immflyretail.inseat.sampleapp.settings.R
import com.immflyretail.inseat.sampleapp.settings_api.SettingsScreenContract
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.B_16_24
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_12
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_14
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle.N_16_24
import com.immflyretail.inseat.sampleapp.ui.AppScaffold
import com.immflyretail.inseat.sampleapp.ui.Loading
import com.immflyretail.inseat.sampleapp.ui.SingleEventEffect

fun NavGraphBuilder.settingsScreen(navController: NavController) {
    composable<SettingsScreenContract.Route> {
        val viewModel: SettingsScreenViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        SettingsScreen(uiState, viewModel, navController)
    }
}

@Composable
private fun SettingsScreen(
    uiState: SettingsScreenState,
    viewModel: SettingsScreenViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    AppScaffold(
        modifier = modifier,
        title = stringResource(R.string.settings),
        onBackClicked = { viewModel.obtainEvent(SettingsScreenEvent.OnBackClicked) },
    ) {

        when (uiState) {
            is SettingsScreenState.DataLoaded -> ContentScreen(
                uiState = uiState,
                eventReceiver = viewModel::obtainEvent,
            )

            SettingsScreenState.Loading -> Loading()
        }
    }

    SingleEventEffect(viewModel.uiAction) { action ->
        when (action) {
            is SettingsScreenAction.Navigate -> navController.execute(action.lambda)
        }
    }
}

@Composable
private fun ContentScreen(
    uiState: SettingsScreenState.DataLoaded,
    eventReceiver: (SettingsScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        RefreshTypeSelector(
            modifier = modifier,
            eventReceiver = eventReceiver,
            isAutoRefreshEnabled = uiState.isAutoRefreshEnabled
        )

        AppInfoSection(
            isDebug = uiState.isDebug,
            appVersion = uiState.appVersion,
            sdkVersion = uiState.sdkVersion,
            environment = uiState.environment,
            supportedICAOs = uiState.supportedICAOs
        )
    }
}

@Composable
private fun RefreshTypeSelector(
    isAutoRefreshEnabled: Boolean,
    eventReceiver: (SettingsScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
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
                text = stringResource(R.string.data_refresh_type),
                style = N_16_24,
                color = MaterialTheme.colorScheme.onBackground,
            )

            val selectedBackground = MaterialTheme.colorScheme.primary
            val unselectedBackground = MaterialTheme.colorScheme.surfaceVariant
            val selectedText = MaterialTheme.colorScheme.onPrimary
            val unselectedText = MaterialTheme.colorScheme.onSurfaceVariant

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
                        .clickable { eventReceiver(SettingsScreenEvent.OnAutoRefreshEnabled) }
                        .background(
                            if (isAutoRefreshEnabled) {
                                selectedBackground
                            } else {
                                unselectedBackground
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.auto),
                        style = N_12,
                        color = if (isAutoRefreshEnabled) {
                            selectedText
                        } else {
                            unselectedText
                        },
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(60.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .clickable { eventReceiver(SettingsScreenEvent.OnManualRefreshEnabled) }
                        .background(
                            if (isAutoRefreshEnabled) {
                                unselectedBackground
                            } else {
                                selectedBackground
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.manual),
                        style = N_12,
                        color = if (isAutoRefreshEnabled) {
                            unselectedText
                        } else {
                            selectedText
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppInfoSection(
    isDebug: Boolean,
    appVersion: String,
    sdkVersion: String,
    environment: String,
    supportedICAOs: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.about),
            style = B_16_24,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        InfoRow(
            label = stringResource(R.string.app_version),
            value = appVersion + if (isDebug) "-debug" else "-release"
        )
        if (isDebug) {
            InfoRow(label = stringResource(R.string.inseat_sdk_version), value = sdkVersion)
            InfoRow(label = stringResource(R.string.environment), value = environment)
            InfoRow(label = stringResource(R.string.icaos), value = supportedICAOs)
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = N_14,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = value,
            style = N_14,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}