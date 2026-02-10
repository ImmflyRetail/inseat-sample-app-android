package com.immflyretail.inseat.sampleapp.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.immflyretail.inseat.sampleapp.ui.utils.IconWrapper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    isBackButtonEnabled: Boolean = true,
    onBackClicked: () -> Unit = {},
    topBarSearch: (@Composable () -> Unit)? = null,
    topBarActions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                showTopBar = showTopBar,
                topBarSearch = topBarSearch,
                title = title,
                isBackButtonEnabled = isBackButtonEnabled,
                onBackClicked = onBackClicked,
                topBarActions = topBarActions
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
        ) {
            content()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppTopBar(
    showTopBar: Boolean,
    topBarSearch: @Composable (() -> Unit)?,
    title: String,
    isBackButtonEnabled: Boolean,
    onBackClicked: () -> Unit,
    topBarActions: @Composable (() -> Unit)?
) {
    if (showTopBar) {
        TopAppBar(
            title = { TopBarTitle(topBarSearch, title) },
            navigationIcon = { TopBarBackButton(isBackButtonEnabled, onBackClicked) },
            actions = { topBarActions?.invoke() },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun TopBarTitle(
    topBarSearch: @Composable (() -> Unit)?,
    title: String
) {
    if (topBarSearch != null) {
        topBarSearch()
    } else {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.animateContentSize()
        )
    }
}

@Composable
private fun TopBarBackButton(isBackButtonEnabled: Boolean, onBackClicked: () -> Unit) {
    if (isBackButtonEnabled) {
        AppIconButton(
            icon = IconWrapper.Vector(Icons.AutoMirrored.Outlined.ArrowBack),
            onClick = onBackClicked,
            modifier = Modifier.animateContentSize()
        )
    }
}
