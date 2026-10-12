package com.example.fiscus.presentation.common.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class ExpenseAddedEvents {
    private val channel = Channel<Unit>(Channel.CONFLATED)
    val events: Flow<Unit> = channel.receiveAsFlow()

    fun notifyAdded() {
        channel.trySend(Unit)
    }
}
