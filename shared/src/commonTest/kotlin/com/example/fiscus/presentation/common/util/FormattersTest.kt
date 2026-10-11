package com.example.fiscus.presentation.common.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class FormattersTest {
    @Test
    fun formatsRupiahWithDotSeparator() {
        assertEquals("Rp 0", formatRupiah(0))
        assertEquals("Rp 500", formatRupiah(500))
        assertEquals("Rp 25.000", formatRupiah(25_000))
        assertEquals("Rp 62.500", formatRupiah(62_500))
        assertEquals("Rp 485.000", formatRupiah(485_000))
        assertEquals("Rp 1.842.500", formatRupiah(1_842_500))
    }

    @Test
    fun formatsNegativeRupiah() {
        assertEquals("-Rp 62.500", formatRupiah(-62_500))
    }

    @Test
    fun formatsShortDateWithIndonesianMonth() {
        assertEquals("8 Okt", formatShortDate(LocalDateTime(2026, 10, 8, 9, 0)))
        assertEquals("30 Sep", formatShortDate(LocalDateTime(2026, 9, 30, 9, 0)))
        assertEquals("1 Mei", formatShortDate(LocalDateTime(2026, 5, 1, 9, 0)))
        assertEquals("17 Agu", formatShortDate(LocalDateTime(2026, 8, 17, 9, 0)))
    }

    @Test
    fun formatsMonthYear() {
        assertEquals("Oktober 2026", formatMonthYear(YearMonth(2026, 10)))
        assertEquals("Januari 2027", formatMonthYear(YearMonth(2027, 1)))
    }

    @Test
    fun formatsShortMonth() {
        assertEquals("Agu", formatShortMonth(YearMonth(2026, 8)))
        assertEquals("Des", formatShortMonth(YearMonth(2026, 12)))
    }
}
