package com.example.fiscus.data.ocr

expect class ReceiptScanner() {
    suspend fun scanText(imageBytes: ByteArray): List<TextFragment>
}