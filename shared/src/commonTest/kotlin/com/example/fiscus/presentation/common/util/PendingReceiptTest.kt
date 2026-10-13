package com.example.fiscus.presentation.common.util

import com.example.fiscus.domain.model.ExpenseSource
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PendingReceiptTest {

    @Test
    fun takeReturnsImageWithItsSource() {
        val pending = PendingReceipt()
        pending.put(byteArrayOf(1, 2, 3), ExpenseSource.OCR_CAMERA)

        val image = assertNotNull(pending.take())

        assertContentEquals(byteArrayOf(1, 2, 3), image.bytes)
        assertEquals(ExpenseSource.OCR_CAMERA, image.source)
    }

    @Test
    fun takeEmptiesThePendingImage() {
        val pending = PendingReceipt()
        pending.put(byteArrayOf(1), ExpenseSource.OCR_GALLERY)

        pending.take()

        assertNull(pending.take())
    }

    @Test
    fun putReplacesPreviousImage() {
        val pending = PendingReceipt()
        pending.put(byteArrayOf(1), ExpenseSource.OCR_GALLERY)
        pending.put(byteArrayOf(2), ExpenseSource.OCR_CAMERA)

        val image = assertNotNull(pending.take())

        assertContentEquals(byteArrayOf(2), image.bytes)
        assertEquals(ExpenseSource.OCR_CAMERA, image.source)
    }
}
