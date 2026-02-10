package com.immflyretail.inseat.sampleapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.immflyretail.inseat.sampleapp.theme.AppTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAnimatedBottomSheet(
    isVisible: Boolean,
    title: String,
    description: String,
    primaryButtonText: String,
    onDismissClicked: () -> Unit,
    onPrimaryButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    imagePainter: Painter? = null,
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null,
) {
    AnimatedBottomSheet(
        isVisible = isVisible,
        onDismissRequest = onDismissClicked,
        dragHandle = null,
        modifier = modifier,
    ) {
        BottomSheetContent(
            title = title,
            description = description,
            primaryButtonText = primaryButtonText,
            onPrimaryButtonClick = onPrimaryButtonClick,
            imagePainter = imagePainter,
            secondaryButtonText = secondaryButtonText,
            onSecondaryButtonClick = onSecondaryButtonClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPersistentBottomSheet(
    title: String,
    description: String,
    primaryButtonText: String,
    onPrimaryButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    imagePainter: Painter? = null,
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )

    ModalBottomSheet(
        onDismissRequest = { },
        sheetState = sheetState,
        dragHandle = null,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        BottomSheetContent(
            title = title,
            description = description,
            primaryButtonText = primaryButtonText,
            onPrimaryButtonClick = onPrimaryButtonClick,
            imagePainter = imagePainter,
            secondaryButtonText = secondaryButtonText,
            onSecondaryButtonClick = onSecondaryButtonClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedBottomSheet(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 0.dp,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    content: @Composable ColumnScope.() -> Unit,
) {
    LaunchedEffect(isVisible) {
        if (isVisible) {
            sheetState.show()
        } else {
            sheetState.hide()
            onDismissRequest()
        }
    }
    if (!sheetState.isVisible && !isVisible) {
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        properties = properties,
        content = content,
    )
}

@Composable
private fun BottomSheetContent(
    title: String,
    description: String,
    primaryButtonText: String,
    onPrimaryButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    imagePainter: Painter? = null,
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (imagePainter != null) {
            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
        Text(
            text = title,
            style = AppTextStyle.B_24_32,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = description,
            style = AppTextStyle.N_16,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (secondaryButtonText != null && onSecondaryButtonClick != null) {
                AppButton(
                    style = ButtonStyle.Outlined,
                    text = secondaryButtonText,
                    onClick = onSecondaryButtonClick,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )

                AppButton(
                    text = primaryButtonText,
                    onClick = onPrimaryButtonClick,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            } else {
                AppButton(
                    text = primaryButtonText,
                    onClick = onPrimaryButtonClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
