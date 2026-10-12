package com.example.fiscus.domain.model

import kotlinx.datetime.YearMonth

data class MonthTotal(
    val month: YearMonth,
    val total: Long
)
