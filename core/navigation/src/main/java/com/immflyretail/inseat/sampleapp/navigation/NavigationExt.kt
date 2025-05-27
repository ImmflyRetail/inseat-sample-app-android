package com.immflyretail.inseat.sampleapp.navigation

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
