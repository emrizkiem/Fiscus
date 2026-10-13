package com.example.fiscus.presentation.common.util

import com.example.fiscus.domain.model.ExpenseSource

class ReceiptImage(val bytes: ByteArray, val source: ExpenseSource)

class PendingReceipt {
    private var pending: ReceiptImage? = null

    fun put(bytes: ByteArray, source: ExpenseSource) {
        pending = ReceiptImage(bytes, source)
    }

    fun take(): ReceiptImage? = pending.also { pending = null }
}
