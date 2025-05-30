package com.immflyretail.inseat.sampleapp

import android.app.Application
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.BASKET
import com.immflyretail.inseat.sampleapp.preferences.api.preferencesmanager.PreferencesManager
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.NetworkException
import com.immflyretail.inseat.sdk.api.models.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

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
                    apiKey = "put-your-api-key-here",
                )
            )

            try {
                api.syncProductData()
            } catch (e: NetworkException) {
                Timber.e("Network error: ${e.message}")
            }

            // cleanup old basket data
            prefManager.remove(BASKET)
        }
    }
}