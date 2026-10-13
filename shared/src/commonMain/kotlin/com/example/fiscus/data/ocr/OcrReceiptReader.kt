package com.example.fiscus.data.ocr

import com.example.fiscus.data.parser.ReceiptParser
import com.example.fiscus.domain.model.ReceiptScanResult
import com.example.fiscus.domain.receipt.ReceiptReader

class OcrReceiptReader(private val scanner: ReceiptScanner) : ReceiptReader {
    override suspend fun read(imageBytes: ByteArray): ReceiptScanResult {
        val parsed = ReceiptParser.parse(assembleLines(scanner.scanText(imageBytes)))
        return ReceiptScanResult(amount = parsed.amount, merchant = parsed.merchant, date = parsed.date)
    }
}
