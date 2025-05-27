package com.immflyretail.inseat.sampleapp.orders.data

import com.immflyretail.inseat.sampleapp.core.DispatchersProvider
import com.immflyretail.inseat.sdk.api.InseatApi
import com.immflyretail.inseat.sdk.api.models.Order
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface OrdersRepository {
    suspend fun observeOrders(): StateFlow<List<Order>>
    suspend fun cancelOrder(orderId: String, callback: (Result<Unit>) -> Unit)
}

internal class OrdersRepositoryImpl @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val inseatApi: InseatApi
) : OrdersRepository {
    override suspend fun observeOrders(): StateFlow<List<Order>> = withContext(dispatchersProvider.getIO()){
        inseatApi.observeOrders()
    }

    override suspend fun cancelOrder(orderId: String, callback: (Result<Unit>) -> Unit) {
        inseatApi.cancelOrder(orderId, callback)
    }
}