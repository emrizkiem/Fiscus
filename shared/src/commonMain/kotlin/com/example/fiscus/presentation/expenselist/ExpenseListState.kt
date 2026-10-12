package com.example.fiscus.presentation.expenselist

import com.example.fiscus.domain.model.Category
import com.example.fiscus.domain.model.Expense
import kotlinx.datetime.YearMonth

data class ExpenseListState(
    val currentMonth: YearMonth,
    val selectedMonth: YearMonth,
    val selectedCategory: Category? = null,
    val monthTotal: Long = 0,
    val monthTransactionCount: Int = 0,
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true
) {
    val isMonthEmpty: Boolean get() = !isLoading && monthTransactionCount == 0
    val isCategoryEmpty: Boolean get() = !isLoading && !isMonthEmpty && expenses.isEmpty()
}
