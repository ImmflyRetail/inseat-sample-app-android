package com.immflyretail.inseat.sampleapp.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import androidx.navigation.NavType
import kotlinx.serialization.json.Json

fun <T> NavController.setResult(
    key: String,
    value: T
) {
    previousBackStackEntry
        ?.savedStateHandle
        ?.set(key, value)
}

@Composable
fun <T> NavController.observeResultAsState(
    key: String
): State<T?>? {
    return currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<T>(key)
        ?.observeAsState()
}

inline fun <reified T : Any> serializableType(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String) =
        bundle.getString(key)?.let<String, T>(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(value)

    override fun serializeAsValue(value: T): String = json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, json.encodeToString(value))
    }
}

/**
 * Attempts to pop the back stack. If empty (probably root screen),
 * it finds and finishes the Activity to exit the app.
 */
fun NavController.popBackStackOrFinish() {
    if (!popBackStack()) {
        context.findActivity()?.finish()
    }
}

/**
 * Recursively unwraps the [Context] to find the underlying [Activity].
 * Essential for Hilt or ThemeWrappers where the context is not a direct Activity instance.
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}