package com.immflyretail.inseat.sampleapp.preferences.impl.di

import android.content.Context
import com.google.gson.Gson
import com.immflyretail.inseat.sampleapp.preferences.impl.preferencesmanager.DataStorePreferencesManagerImpl
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal class PreferencesModule {

    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun providePreferencesManager(
        @ApplicationContext context: Context,
        gson: Gson,
    ): PreferencesManager {
        return DataStorePreferencesManagerImpl(context, gson)
    }
}