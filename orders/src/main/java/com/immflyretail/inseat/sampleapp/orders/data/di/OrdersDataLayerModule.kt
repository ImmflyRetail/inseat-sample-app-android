package com.immflyretail.inseat.sampleapp.orders.data.di

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sampleapp.orders.data.OrdersRepository
import com.immflyretail.inseat.sampleapp.orders.data.OrdersRepositoryImpl
import com.immflyretail.inseat.sdk.api.InseatApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal class OrdersDataLayerModule {

    @Provides
    fun provideOrdersRepository(): OrdersRepository {
        return OrdersRepositoryImpl(DispatchersProvider(), InseatApi.getInstance())
    }
}