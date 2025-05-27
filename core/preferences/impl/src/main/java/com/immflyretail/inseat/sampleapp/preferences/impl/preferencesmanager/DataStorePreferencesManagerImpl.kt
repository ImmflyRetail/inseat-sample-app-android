package com.immflyretail.inseat.sampleapp.preferences.impl.preferencesmanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("inseat_datastore")

internal class DataStorePreferencesManagerImpl @Inject constructor(
    private val context: Context,
    private val gson: Gson,
) : PreferencesManager {

    private val dataStore by lazy { context.dataStore }

    override suspend fun write(key: String, data: String) {
        dataStore.put(stringPreferencesKey(key), data)
    }

    override suspend fun write(key: String, data: Int) {
        dataStore.put(intPreferencesKey(key), data)
    }

    override suspend fun write(key: String, data: Long) {
        dataStore.put(longPreferencesKey(key), data)
    }

    override suspend fun write(key: String, data: Float) {
        dataStore.put(floatPreferencesKey(key), data)
    }

    override suspend fun write(key: String, data: Boolean) {
        dataStore.put(booleanPreferencesKey(key), data)
    }

    override suspend fun write(key: String, data: Any) {
        val jsonString = gson.toJson(data)
        dataStore.put(stringPreferencesKey(key), jsonString)
    }

    override suspend fun read(key: String, defaultValue: String): String {
        return dataStore.data.first()[stringPreferencesKey(key)] ?: defaultValue
    }

    override suspend fun read(key: String, defaultValue: Int): Int {
        return dataStore.data.first()[intPreferencesKey(key)] ?: defaultValue
    }

    override suspend fun read(key: String, defaultValue: Long): Long {
        return dataStore.data.first()[longPreferencesKey(key)] ?: defaultValue
    }

    override suspend fun read(key: String, defaultValue: Float): Float {
        return dataStore.data.first()[floatPreferencesKey(key)] ?: defaultValue
    }

    override suspend fun read(key: String, defaultValue: Boolean): Boolean {
        return dataStore.data.first()[booleanPreferencesKey(key)] ?: defaultValue
    }

    override suspend fun <T> read(key: String, clazz: Class<T>): T? {
        return try {
            val savedJson = dataStore.data.first()[stringPreferencesKey(key)].orEmpty()
            gson.fromJson(savedJson, clazz)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun remove(key: String) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                when {
                    it.contains(stringPreferencesKey(key)) -> remove(stringPreferencesKey(key))
                    it.contains(intPreferencesKey(key)) -> remove(intPreferencesKey(key))
                    it.contains(longPreferencesKey(key)) -> remove(longPreferencesKey(key))
                    it.contains(floatPreferencesKey(key)) -> remove(floatPreferencesKey(key))
                    it.contains(booleanPreferencesKey(key)) -> remove(booleanPreferencesKey(key))
                }
            }
        }
    }

    private suspend fun <T : Any> DataStore<Preferences>.put(key: Preferences.Key<T>, data: T) {
        this.updateData {
            it.toMutablePreferences().apply {
                this[key] = data
            }
        }
    }
}