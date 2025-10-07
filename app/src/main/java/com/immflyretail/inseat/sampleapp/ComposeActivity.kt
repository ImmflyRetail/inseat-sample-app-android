package com.immflyretail.inseat.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.immflyretail.inseat.sampleapp.ui.theme.ImmseatTheme
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.NetworkException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        var keepSplashScreen = true
        super.onCreate(savedInstanceState)
        splashscreen.setKeepOnScreenCondition { keepSplashScreen }

        val api = InseatApi.getInstance()
        api.checkPermissions(this@ComposeActivity) {
            lifecycleScope.launch {
                try {
                    api.syncProductData()
                } catch (e: NetworkException) {
                    Timber.e("Network error: ${e.message}")
                }

                api.start()
                keepSplashScreen = false

                setContent { ImmseatTheme { NavigationHost() } }
            }
        }
    }
}