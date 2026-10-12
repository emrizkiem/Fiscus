package com.example.fiscus.presentation.summary

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.fiscus.domain.repository.ExpenseRepository
import com.example.fiscus.presentation.common.util.SelectedMonthStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.YearMonth

private const val TREND_MONTH_COUNT = 5

class SummaryScreenModel(
    private val repository: ExpenseRepository,
    private val currentMonth: YearMonth,
    private val monthStore: SelectedMonthStore,
) : ScreenModel {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<SummaryState> =
        monthStore.selectedMonth.flatMapLatest { month ->
            combine(
                repository.getCategoryTotals(month),
                repository.getMonthlyTotals(endMonth = month, count = TREND_MONTH_COUNT),
            ) { categoryTotals, monthlyTotals ->
                SummaryState(
                    currentMonth = currentMonth,
                    selectedMonth = month,
                    monthTotal = categoryTotals.sumOf { it.total },
                    monthlyTotals = monthlyTotals,
                    categoryTotals = categoryTotals,
                    isLoading = false,
                )
            }
        }.stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SummaryState(currentMonth = currentMonth, selectedMonth = monthStore.selectedMonth.value),
        )

    fun selectMonth(month: YearMonth) {
        monthStore.select(month)
    }
}
