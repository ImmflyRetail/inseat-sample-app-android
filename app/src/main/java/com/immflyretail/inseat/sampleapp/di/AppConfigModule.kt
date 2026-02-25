package com.immflyretail.inseat.sampleapp.di

import com.immflyretail.inseat.sampleapp.BuildConfig
import com.immflyretail.inseat.sampleapp.core.AppConfig
import com.immflyretail.inseat.sampleapp.core.AppEnvironment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig {
        return AppConfig(
            isDebug = BuildConfig.DEBUG,
            appVersion = BuildConfig.VERSION_NAME,
            sdkVersion = BuildConfig.INSEAT_SDK_VERSION,
            supportedICAOs = BuildConfig.SUPPORTED_ICAOS.split(","),
            environment = AppEnvironment.TEST,
        )
    }
}