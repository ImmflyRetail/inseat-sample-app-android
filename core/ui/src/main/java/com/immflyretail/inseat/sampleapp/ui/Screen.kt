package com.immflyretail.inseat.sampleapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Screen(
    modifier: Modifier = Modifier,
    title: String,
    toolbarItem: @Composable () -> Unit = {},
    customToolbar: (@Composable () -> Unit)? = null,
    isBackButtonEnabled: Boolean = true,
    hasTopBar: Boolean = true,
    onBackClicked: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Scaffold(
        topBar = {
            if (hasTopBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    if (isBackButtonEnabled) {
                        Image(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .align(Alignment.CenterStart)
                                .clickable { onBackClicked() },
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = null
                        )
                    }

                    if (customToolbar != null) {
                        customToolbar()
                    } else {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            text = title,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            toolbarItem()
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}