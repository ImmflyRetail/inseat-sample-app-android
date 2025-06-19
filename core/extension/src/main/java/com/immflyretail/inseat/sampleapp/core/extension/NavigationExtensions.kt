package com.immflyretail.inseat.sampleapp.core.extension

import androidx.navigation.NavController

fun NavController.execute(lambda: NavController.() -> Unit) = lambda.invoke(this)