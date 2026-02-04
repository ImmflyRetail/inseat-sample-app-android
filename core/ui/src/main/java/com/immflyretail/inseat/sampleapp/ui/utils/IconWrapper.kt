package com.immflyretail.inseat.sampleapp.ui.utils

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

sealed class IconWrapper {
    data class Vector(val imageVector: ImageVector) : IconWrapper()
    data class Drawable(@param:DrawableRes val id: Int) : IconWrapper()
}