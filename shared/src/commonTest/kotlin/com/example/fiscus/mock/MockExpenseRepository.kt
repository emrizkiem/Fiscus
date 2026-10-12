package com.example.fiscus.mock

import com.example.fiscus.domain.model.CategoryTotal
import com.example.fiscus.domain.model.Expense
import com.example.fiscus.domain.model.MonthTotal
import com.example.fiscus.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth

class MockExpenseRepository(initial: List<Expense> = emptyList()) : ExpenseRepository {
    private val expenses = MutableStateFlow(initial)

    override fun getExpensesByMonth(yearMonth: YearMonth): Flow<List<Expense>> =
        expenses.map { all ->
            all.filter { it.isIn(yearMonth) }
                .sortedByDescending { it.date }
        }

    override suspend fun addExpense(expense: Expense) {
        expenses.update { it + expense }
    }

    override suspend fun getExpenseById(id: String): Expense? = expenses.value.find { it.id == id }

    override suspend fun updateExpense(expense: Expense) {
        expenses.update { all -> all.map { if (it.id == expense.id) expense else it } }
    }

    override suspend fun deleteExpense(id: String) {
        expenses.update { all -> all.filterNot { it.id == id } }
    }

    override fun getCategoryTotals(yearMonth: YearMonth): Flow<List<CategoryTotal>> =
        expenses.map { all ->
            all.filter { it.isIn(yearMonth) }
                .groupBy { it.category }
                .map { (category, items) -> CategoryTotal(category, items.sumOf { it.amount }) }
                .sortedByDescending { it.total }
        }

    override fun getMonthlyTotals(endMonth: YearMonth, count: Int): Flow<List<MonthTotal>> =
        expenses.map { all ->
            generateSequence(endMonth) { it.minusMonth() }.take(count).toList().reversed()
                .map { month -> MonthTotal(month, all.filter { it.isIn(month) }.sumOf { it.amount }) }
        }

    private fun Expense.isIn(month: YearMonth) = date.year == month.year && date.month == month.month
}
