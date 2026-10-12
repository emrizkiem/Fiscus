package com.example.fiscus.presentation.common.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.YearMonth

class SelectedMonthStore(initialMonth: YearMonth) {
    private val _selectedMonth = MutableStateFlow(initialMonth)
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    fun select(month: YearMonth) {
        _selectedMonth.value = month
    }
}
