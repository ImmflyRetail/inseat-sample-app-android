package com.immflyretail.inseat.sampleapp.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.immflyretail.inseat.sampleapp.ui.InseatTextStyle.B_16

@Composable
fun InseatButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    icon: @Composable () -> Unit = {}
) {
    Button(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth(),
        onClick = { onClick.invoke() },
        shape = RoundedCornerShape(size = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFDD083A),
            disabledContainerColor = Color(0xFFF19CB0)
        ),
        enabled = isEnabled,

    ) {
        icon()
        Text(
            text = text,
            style = B_16,
            color = Color(0xFFFFFFFF),
            textAlign = TextAlign.Center,
        )
    }
}