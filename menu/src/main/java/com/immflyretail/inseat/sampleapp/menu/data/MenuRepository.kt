package com.immflyretail.inseat.sampleapp.menu.data

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.AUTO_REFRESH
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.Menu
import com.immflyretail.inseat.sdk.api.models.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface MenuRepository {
    suspend fun isAutoupdateEnabled(): Flow<Boolean>
    suspend fun selectMenu(menu: Menu)
    suspend fun getAvailableMenus(): List<Menu>
}

internal class ListRepositoryImpl @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val inseatApi: InseatApi,
    private val preferencesManager: PreferencesManager
) : MenuRepository {

    override suspend fun isAutoupdateEnabled(): Flow<Boolean> {
        return preferencesManager.asBooleanFlow(AUTO_REFRESH, true)
    }

    override suspend fun selectMenu(menu: Menu) {
        inseatApi.setUserData(UserData(menu))
    }

    override suspend fun getAvailableMenus(): List<Menu> = withContext(dispatchersProvider.getIO()) {
        inseatApi.fetchMenus().sortedBy { it.key }
    }
}