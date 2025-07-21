package com.immflyretail.inseat.sampleapp.core.extension

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

@Composable
fun String.toBitmapPainter(@DrawableRes defaultImgRes: Int): Painter {
    val decodedBytes = Base64.decode(this, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

    return if (bitmap != null) {
        BitmapPainter(image = bitmap.asImageBitmap())
    } else {
        painterResource(defaultImgRes)
    }
}

