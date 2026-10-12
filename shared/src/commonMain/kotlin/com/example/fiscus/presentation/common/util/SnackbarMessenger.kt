package com.example.fiscus.presentation.common.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

data class SnackbarMessage(
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)

class SnackbarMessenger {
    private val channel = Channel<SnackbarMessage>(Channel.BUFFERED)
    val messages: Flow<SnackbarMessage> = channel.receiveAsFlow()

    fun show(text: String) = show(SnackbarMessage(text))

    fun show(message: SnackbarMessage) {
        channel.trySend(message)
    }
}
