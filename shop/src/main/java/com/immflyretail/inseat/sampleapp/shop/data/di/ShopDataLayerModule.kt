package com.immflyretail.inseat.sampleapp.shop.data.di

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sampleapp.shop.data.ListRepositoryImpl
import com.immflyretail.inseat.sampleapp.shop.data.ShopRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal class ShopDataLayerModule {

    @Provides
    fun provideList(
        preferencesManager: PreferencesManager
    ): ShopRepository {
        return ListRepositoryImpl(DispatchersProvider(), InseatApi.getInstance(), preferencesManager)
    }
}
