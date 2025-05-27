package com.immflyretail.inseat.sampleapp.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DispatchersProvider {
    fun getMain(): CoroutineDispatcher = Dispatchers.Main
    fun getIO(): CoroutineDispatcher = Dispatchers.IO
}