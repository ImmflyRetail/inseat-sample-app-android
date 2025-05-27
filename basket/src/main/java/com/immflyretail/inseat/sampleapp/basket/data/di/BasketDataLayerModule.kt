package com.immflyretail.inseat.sampleapp.basket.data.di

import com.immflyretail.inseat.sampleapp.basket.data.BasketRepository
import com.immflyretail.inseat.sampleapp.basket.data.BasketRepositoryImpl
import com.immflyretail.inseat.sampleapp.basket.data.CheckoutRepository
import com.immflyretail.inseat.sampleapp.basket.data.CheckoutRepositoryImpl
import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@InstallIn(SingletonComponent::class)
@Module
internal class BasketDataLayerModule {

    @Provides
    fun provideBasketRepository(
        preferencesManager: PreferencesManager
    ): BasketRepository {
        return BasketRepositoryImpl(DispatchersProvider(), InseatApi.getInstance(), preferencesManager)
    }

    @Provides
    fun provideCheckoutRepository(
        preferencesManager: PreferencesManager
    ): CheckoutRepository {
        return CheckoutRepositoryImpl(DispatchersProvider(), InseatApi.getInstance(), preferencesManager)
    }
}