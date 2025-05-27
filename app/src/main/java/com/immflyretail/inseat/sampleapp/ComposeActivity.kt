package com.immflyretail.inseat.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sampleapp.ui.theme.ImmseatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = InseatApi.getInstance()
        api.checkPermissions(this) {
            api.start()
            setContent {
                ImmseatTheme {
                    NavigationHost()
                }
            }
        }
    }
}