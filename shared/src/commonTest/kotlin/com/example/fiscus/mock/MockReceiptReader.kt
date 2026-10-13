package com.example.fiscus.mock

import com.example.fiscus.domain.model.ReceiptScanResult
import com.example.fiscus.domain.receipt.ReceiptReader

class MockReceiptReader(
    private val result: ReceiptScanResult? = ReceiptScanResult(amount = null, merchant = null, date = null),
) : ReceiptReader {
    var readCount = 0
        private set

    override suspend fun read(imageBytes: ByteArray): ReceiptScanResult {
        readCount++
        return result ?: error("OCR gagal")
    }
}
