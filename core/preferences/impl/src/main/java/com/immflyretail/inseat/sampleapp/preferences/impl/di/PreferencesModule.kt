package com.immflyretail.inseat.sampleapp.preferences.impl.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.immflyretail.inseat.sampleapp.preferences.impl.preferencesmanager.DataStorePreferencesManagerImpl
import com.immflyretail.inseat.sampleapp.preferences.impl.preferencesmanager.Encripted
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.Secured
import com.immflyretail.inseat.sampleapp.preferences.impl.preferencesmanager.SecuredPreferencesManager
import com.immflyretail.inseat.sampleapp.preferences.impl.preferencesmanager.SecuritySharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val SETTINGS_FILE_NAME = "skeleton_setting_name"

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

    @Singleton
    @Provides
    @Encripted
    fun provideSecuredSharedPreferences(
        @ApplicationContext context: Context,
    ): SharedPreferences {
        return SecuritySharedPreferences.create(context, SETTINGS_FILE_NAME)
    }

    @Singleton
    @Provides
    @Secured
    fun provideSecuredPreferences(
        @Encripted sharedPreferences: SharedPreferences,
        gson: Gson,
    ): PreferencesManager {
        return SecuredPreferencesManager(
            sharedPreferences,
            gson,
        )
    }
}