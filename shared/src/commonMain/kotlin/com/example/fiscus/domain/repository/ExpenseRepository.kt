package com.example.fiscus.domain.repository

import com.example.fiscus.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

interface ExpenseRepository {
    fun getExpensesByMonth(yearMonth: YearMonth): Flow<List<Expense>>
    suspend fun addExpense(expense: Expense)
    suspend fun getExpenseById(id: String): Expense?
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
}