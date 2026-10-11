package com.example.fiscus.data.local

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

object LocalDateTimeAdapter : ColumnAdapter<LocalDateTime, String> {
    private val format = LocalDateTime.Format {
        date(LocalDate.Formats.ISO)
        char('T')
        hour()
        char(':')
        minute()
        char(':')
        second()
    }

    override fun decode(databaseValue: String): LocalDateTime = format.parse(databaseValue)

    override fun encode(value: LocalDateTime): String = format.format(value)
}