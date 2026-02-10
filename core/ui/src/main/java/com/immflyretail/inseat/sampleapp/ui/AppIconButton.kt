package com.immflyretail.inseat.sampleapp.ui

import androidx.annotation.StringRes
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.immflyretail.inseat.sampleapp.ui.utils.IconWrapper

@Composable
fun AppIconButton(
    icon: IconWrapper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    @StringRes contentDescriptionId: Int? = null,
) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = modifier,
    ) {
        AppIcon(icon = icon, contentDescriptionId = contentDescriptionId)
    }
}