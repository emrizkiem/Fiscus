package com.example.fiscus.data.ocr

import kotlin.test.Test
import kotlin.test.assertEquals

class ReceiptTextAssemblerTest {

    private fun fragment(text: String, left: Float, top: Float, height: Float = 0.01f) =
        TextFragment(text = text, left = left, top = top, bottom = top + height)

    @Test
    fun fragmentsOnSameRowAreJoinedLeftToRight() {
        val lines = assembleLines(
            listOf(
                fragment("127,600", left = 0.85f, top = 0.502f),
                fragment("Total Belanja", left = 0.05f, top = 0.500f),
            ),
        )

        assertEquals(listOf("Total Belanja 127,600"), lines)
    }

    @Test
    fun rowsAreOrderedTopToBottom() {
        val lines = assembleLines(
            listOf(
                fragment("Kembalian", left = 0.05f, top = 0.60f),
                fragment("Alfamart", left = 0.40f, top = 0.02f),
                fragment("Total Belanja", left = 0.05f, top = 0.50f),
            ),
        )

        assertEquals(listOf("Alfamart", "Total Belanja", "Kembalian"), lines)
    }

    @Test
    fun tableRowWithManyColumnsStaysTogether() {
        val lines = assembleLines(
            listOf(
                fragment("9,100", left = 0.90f, top = 0.2004f),
                fragment("GURIBEE RL 65G", left = 0.03f, top = 0.2000f),
                fragment("9,100", left = 0.70f, top = 0.2006f),
                fragment("1", left = 0.55f, top = 0.2002f),
            ),
        )

        assertEquals(listOf("GURIBEE RL 65G 1 9,100 9,100"), lines)
    }

    @Test
    fun blankFragmentsAreIgnored() {
        val lines = assembleLines(
            listOf(
                fragment("  ", left = 0.5f, top = 0.1f),
                fragment("PPN", left = 0.05f, top = 0.3f),
            ),
        )

        assertEquals(listOf("PPN"), lines)
    }

    @Test
    fun alfamartReceiptIsRebuiltIntoReadableLines() {
        val fragments = listOf(
            fragment("Bon 2G54-255-1809Y9T3", left = 0.03f, top = 0.100f),
            fragment("127,600", left = 0.88f, top = 0.401f),
            fragment("Alfamart", left = 0.38f, top = 0.010f),
            fragment("Total Belanja", left = 0.03f, top = 0.400f),
            fragment("Kasir : HELMA", left = 0.78f, top = 0.101f),
            fragment("Total Item", left = 0.03f, top = 0.380f),
            fragment("11", left = 0.66f, top = 0.381f),
            fragment("133,000", left = 0.88f, top = 0.379f),
            fragment("Tgl. 18-09-2026 10:58:22 V.2026.7.2", left = 0.20f, top = 0.470f),
            fragment("QRIS SOUNDBOX", left = 0.03f, top = 0.420f),
            fragment("127,600", left = 0.88f, top = 0.421f),
        )

        assertEquals(
            listOf(
                "Alfamart",
                "Bon 2G54-255-1809Y9T3 Kasir : HELMA",
                "Total Item 11 133,000",
                "Total Belanja 127,600",
                "QRIS SOUNDBOX 127,600",
                "Tgl. 18-09-2026 10:58:22 V.2026.7.2",
            ),
            assembleLines(fragments),
        )
    }
}
