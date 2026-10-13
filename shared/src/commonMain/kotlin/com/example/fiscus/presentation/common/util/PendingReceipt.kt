package com.example.fiscus.presentation.common.util

class PendingReceipt {
    private var imageBytes: ByteArray? = null

    fun put(bytes: ByteArray) {
        imageBytes = bytes
    }

    fun take(): ByteArray? = imageBytes.also { imageBytes = null }
}
