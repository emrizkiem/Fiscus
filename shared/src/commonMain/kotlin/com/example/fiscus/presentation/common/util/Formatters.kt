package com.example.fiscus.presentation.common.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth

private val shortMonthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des",
)

private val fullMonthNames = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember",
)

fun formatRupiah(amount: Long): String {
    val digits = amount.toString().removePrefix("-")
    val grouped = digits.reversed().chunked(3).joinToString(".").reversed()
    return if (amount < 0) "-Rp $grouped" else "Rp $grouped"
}

fun formatShortDate(date: LocalDateTime): String =
    "${date.day} ${shortMonthNames[date.month.ordinal]}"

fun formatShortMonth(yearMonth: YearMonth): String = shortMonthNames[yearMonth.month.ordinal]

fun formatMonthYear(yearMonth: YearMonth): String =
    "${fullMonthNames[yearMonth.month.ordinal]} ${yearMonth.year}"
