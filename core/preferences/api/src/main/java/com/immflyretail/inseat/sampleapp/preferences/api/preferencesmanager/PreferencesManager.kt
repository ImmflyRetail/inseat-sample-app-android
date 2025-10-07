package com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager

import kotlinx.coroutines.flow.Flow


interface PreferencesManager {

    suspend fun write(key: String, data: String)
    suspend fun write(key: String, data: Int)
    suspend fun write(key: String, data: Long)
    suspend fun write(key: String, data: Float)
    suspend fun write(key: String, data: Boolean)
    suspend fun write(key: String, data: Any)

    suspend fun read(key: String, defaultValue: String): String
    suspend fun read(key: String, defaultValue: Int): Int
    suspend fun read(key: String, defaultValue: Long): Long
    suspend fun read(key: String, defaultValue: Float): Float
    suspend fun read(key: String, defaultValue: Boolean): Boolean
    suspend fun <T> read(key: String, clazz: Class<T>): T?

    suspend fun remove(key: String)

    fun asFlow(key: String, defaultValue: String): Flow<String>
}