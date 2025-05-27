package com.immflyretail.inseat.sampleapp.settings.data.di

import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sampleapp.settings.data.SettingsRepository
import com.immflyretail.inseat.sampleapp.settings.data.SettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal class SettingsDataLayerModule {

    @Provides
    fun provideSettingsRepository(
        preferencesManager: PreferencesManager
    ): SettingsRepository {
        return SettingsRepositoryImpl(preferencesManager)
    }
}