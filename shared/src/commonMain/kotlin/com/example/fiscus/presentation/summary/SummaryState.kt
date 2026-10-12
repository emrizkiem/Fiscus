package com.example.fiscus.presentation.summary

import com.example.fiscus.domain.model.CategoryTotal
import com.example.fiscus.domain.model.MonthTotal
import kotlinx.datetime.YearMonth

data class SummaryState(
    val currentMonth: YearMonth,
    val selectedMonth: YearMonth,
    val monthTotal: Long = 0,
    val monthlyTotals: List<MonthTotal> = emptyList(),
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val isLoading: Boolean = true,
) {
    val isMonthEmpty: Boolean get() = !isLoading && categoryTotals.isEmpty()
}
