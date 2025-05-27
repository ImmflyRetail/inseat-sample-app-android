package com.immflyretail.inseat.sampleapp.preferences.impl.preferencesmanager

import android.content.SharedPreferences
import com.google.gson.Gson
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.Secured
import javax.inject.Inject

private const val DEFAULT_STRING: String = ""

internal class SecuredPreferencesManager @Inject constructor(
    @Secured private val securedPreferences: SharedPreferences,
    private val gson: Gson,
) : PreferencesManager {


    override suspend fun write(key: String, data: String) = securedPreferences.put(key, data)

    override suspend fun write(key: String, data: Int) = securedPreferences.put(key, data)

    override suspend fun write(key: String, data: Long) = securedPreferences.put(key, data)

    override suspend fun write(key: String, data: Float) = securedPreferences.put(key, data)

    override suspend fun write(key: String, data: Boolean) = securedPreferences.put(key, data)

    override suspend fun write(key: String, data: Any) {
        val jsonString = gson.toJson(data)
        securedPreferences.put(key, jsonString)
9    }

    override suspend fun read(key: String, defaultValue: String): String {
        return securedPreferences.read(key, defaultValue)
    }

    override suspend fun read(key: String, defaultValue: Int): Int {
        return securedPreferences.read(key, defaultValue)
    }

    override suspend fun read(key: String, defaultValue: Long): Long {
        return securedPreferences.read(key, defaultValue)
    }

    override suspend fun read(key: String, defaultValue: Float): Float {
        return securedPreferences.read(key, defaultValue)
    }

    override suspend fun read(key: String, defaultValue: Boolean): Boolean {
        return securedPreferences.read(key, defaultValue)
    }

    override suspend fun <T> read(key: String, clazz: Class<T>): T? {
        return try {
            val savedJson = securedPreferences.read(key, DEFAULT_STRING)
            gson.fromJson(savedJson, clazz)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun remove(key: String) {
        securedPreferences.edit().remove(key).apply()
    }
}

internal inline fun <reified T> SharedPreferences.put(key: String, data: T) {
    edit().apply {
        when (T::class) {
            String::class -> putString(key, data as String)
            Int::class -> putInt(key, data as Int)
            Long::class -> putLong(key, data as Long)
            Float::class -> putFloat(key, data as Float)
            Boolean::class -> putBoolean(key, data as Boolean)
            else -> throw IllegalArgumentException("Type not supported: ${T::class.simpleName}")
        }
        apply()
    }
}

internal inline fun <reified T> SharedPreferences.read(key: String, defaultValue: T): T {
    return if (contains(key)) {
        when (T::class) {
            String::class -> getString(key, defaultValue as String) as T
            Int::class -> getInt(key, defaultValue as Int) as T
            Long::class -> getLong(key, defaultValue as Long) as T
            Float::class -> getFloat(key, defaultValue as Float) as T
            Boolean::class -> getBoolean(key, defaultValue as Boolean) as T
            else -> throw IllegalArgumentException("Type not supported: ${T::class.simpleName}")
        }
    } else defaultValue
}