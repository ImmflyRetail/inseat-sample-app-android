package com.immflyretail.inseat.sampleapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_16
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.N_14
import com.immflyretail.inseat.sampleapp.ui.utils.IconWrapper

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Filled,
    isEnabled: Boolean = true,
    leadingIcon: IconWrapper? = null,
    trailingIcon: IconWrapper? = null,
) {
    val commonModifier = modifier.heightIn(min = 48.dp)

    when (style) {
        ButtonStyle.Filled -> {
            Button(
                modifier = commonModifier,
                onClick = onClick,
                enabled = isEnabled,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDD083A),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFF19CB0),
                    disabledContentColor = Color.White
                ),
            ) {
                ButtonContent(
                    text = text,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    textStyle = B_16
                )
            }
        }
        ButtonStyle.Outlined -> {
            val contentColor = if (isEnabled) Color(0xFFDD083A) else Color(0xFFF19CB0)
            OutlinedButton(
                modifier = commonModifier,
                onClick = onClick,
                enabled = isEnabled,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, contentColor),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = Color(0xFFF19CB0)
                ),
            ) {
                ButtonContent(
                    text = text,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    textStyle = B_16
                )
            }
        }
        ButtonStyle.Flat -> {
            TextButton(
                modifier = commonModifier,
                onClick = onClick,
                enabled = isEnabled,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFDD083A),
                    disabledContentColor = Color(0xFFF19CB0)
                ),
            ) {
                ButtonContent(
                    text = text,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    textStyle = B_16
                )
            }
        }
        ButtonStyle.Link -> {
            TextButton(
                modifier = commonModifier,
                onClick = onClick,
                enabled = isEnabled,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFDD083A),
                    disabledContentColor = Color(0xFFF19CB0)
                ),
            ) {
                ButtonContent(
                    text = text,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    textStyle = N_14,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: IconWrapper?,
    trailingIcon: IconWrapper?,
    textStyle: TextStyle,
    textDecoration: TextDecoration? = null
) {
    val contentColor = LocalContentColor.current
    val fontSize = if (textStyle.fontSize.isUnspecified) 16.sp else textStyle.fontSize
    val dynamicIconSize: Dp = with(LocalDensity.current) { (fontSize * 1.25).toDp() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            AppIcon(
                icon = leadingIcon,
                tint = contentColor,
                modifier = Modifier.size(dynamicIconSize)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = text,
            style = textStyle,
            color = contentColor,
            textAlign = TextAlign.Center,
            textDecoration = textDecoration,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(4.dp))
            AppIcon(
                icon = trailingIcon,
                tint = contentColor,
                modifier = Modifier.size(dynamicIconSize)
            )
        }
    }
}

enum class ButtonStyle {
    Filled, Outlined, Flat, Link
}