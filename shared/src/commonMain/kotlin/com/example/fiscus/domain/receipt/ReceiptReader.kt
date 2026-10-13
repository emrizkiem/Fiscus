package com.example.fiscus.domain.receipt

import com.example.fiscus.domain.model.ReceiptScanResult

interface ReceiptReader {
    suspend fun read(imageBytes: ByteArray): ReceiptScanResult
}
