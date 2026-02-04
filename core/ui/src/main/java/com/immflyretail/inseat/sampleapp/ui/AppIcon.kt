package com.immflyretail.inseat.sampleapp.ui

import androidx.compose.ui.graphics.Color
import androidx.annotation.StringRes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.immflyretail.inseat.sampleapp.ui.utils.IconWrapper

@Composable
fun AppIcon(
    icon: IconWrapper,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    @StringRes contentDescriptionId: Int? = null,
) {
    val description = contentDescriptionId?.let { stringResource(it) }

    when (icon) {
        is IconWrapper.Vector -> Icon(
            imageVector = icon.imageVector,
            contentDescription = description,
            modifier = modifier,
            tint = tint
        )

        is IconWrapper.Drawable -> Icon(
            painter = painterResource(icon.id),
            contentDescription = description,
            modifier = modifier,
            tint = tint
        )
    }
}