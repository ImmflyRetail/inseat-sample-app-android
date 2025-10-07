package com.immflyretail.inseat.sampleapp

import android.app.Application
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.BASKET
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import com.immflyretail.inseat.sdk.api.models.Environment

@HiltAndroidApp
class MainApplication : Application() {

    @Inject
    lateinit var prefManager: PreferencesManager

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        runBlocking {
            val api = InseatApi.getInstance()
            api.initialize(
                applicationContext = this@MainApplication,
                configuration = Configuration(
                    apiKey = BuildConfig.API_KEY,
                    supportedICAOs = listOf("WZZ", "WAZ"),
                    environment = Environment.TEST,
                )
            )

            // cleanup old basket data
            prefManager.remove(BASKET)
        }
    }
}