package com.immflyretail.inseat.sampleapp.checkout.data.di

import com.immflyretail.inseat.sampleapp.checkout.data.CheckoutRepository
import com.immflyretail.inseat.sampleapp.checkout.data.CheckoutRepositoryImpl
import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@InstallIn(SingletonComponent::class)
@Module
internal class CheckoutDataLayerModule {

    @Provides
    fun provideCheckoutRepository(
        preferencesManager: PreferencesManager
    ): CheckoutRepository {
        return CheckoutRepositoryImpl(DispatchersProvider(), InseatApi.getInstance(), preferencesManager)
    }
}