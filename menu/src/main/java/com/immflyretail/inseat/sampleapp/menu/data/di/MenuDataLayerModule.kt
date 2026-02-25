package com.immflyretail.inseat.sampleapp.menu.data.di

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sampleapp.menu.data.ListRepositoryImpl
import com.immflyretail.inseat.sampleapp.menu.data.MenuRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal class MenuDataLayerModule {

    @Provides
    fun provideList(
        preferencesManager: PreferencesManager
    ): MenuRepository {
        return ListRepositoryImpl(DispatchersProvider(), InseatApi.getInstance(), preferencesManager)
    }
}
