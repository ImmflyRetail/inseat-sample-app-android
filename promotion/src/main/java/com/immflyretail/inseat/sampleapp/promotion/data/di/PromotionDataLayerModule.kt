package com.immflyretail.inseat.sampleapp.promotion.data.di

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sampleapp.promotion.data.PromotionRepositoryImpl
import com.immflyretail.inseat.sampleapp.promotion.data.PromotionRepository
import com.immflyretail.inseat.sdk.api.InseatApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal class PromotionDataLayerModule {

    @Provides
    fun providePromotionRepository(preferencesManager: PreferencesManager): PromotionRepository {
        return PromotionRepositoryImpl(DispatchersProvider(), InseatApi.getInstance(), preferencesManager)
    }
}
