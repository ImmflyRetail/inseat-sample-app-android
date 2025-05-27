package com.immflyretail.inseat.sampleapp.settings.data

import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.AUTO_REFRESH
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import javax.inject.Inject

interface SettingsRepository {
    suspend fun getAutoRefreshState(): Boolean
    suspend fun setAutoRefreshState(isEnabled: Boolean)
}

internal class SettingsRepositoryImpl @Inject constructor(
    private val prefManager: PreferencesManager
) : SettingsRepository {

    override suspend fun getAutoRefreshState(): Boolean {
        return prefManager.read(AUTO_REFRESH, true)
    }

    override suspend fun setAutoRefreshState(isEnabled: Boolean) {
        return prefManager.write(AUTO_REFRESH, isEnabled)
    }
}