package com.example.fiscus.presentation.expenselist

import com.example.fiscus.domain.model.Expense
import com.example.fiscus.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.YearMonth

class FakeExpenseRepository(initial: List<Expense> = emptyList()) : ExpenseRepository {
    private val expenses = MutableStateFlow(initial)

    override fun getExpensesByMonth(yearMonth: YearMonth): Flow<List<Expense>> =
        expenses.map { all ->
            all.filter { it.date.year == yearMonth.year && it.date.month == yearMonth.month }
                .sortedByDescending { it.date }
        }

    override suspend fun addExpense(expense: Expense) {
        expenses.update { it + expense }
    }
}
