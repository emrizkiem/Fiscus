package com.example.fiscus.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.fiscus.data.local.ExpenseEntity
import com.example.fiscus.data.local.FiscusDatabase
import com.example.fiscus.domain.model.Expense
import com.example.fiscus.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atTime
import kotlinx.datetime.plusMonth

class ExpenseRepositoryImpl(
    database: FiscusDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : ExpenseRepository {
    private val queries = database.expenseQueries

    override fun getExpensesByMonth(yearMonth: YearMonth): Flow<List<Expense>> {
        val start = yearMonth.firstDay.atTime(0, 0)
        val end = yearMonth.plusMonth().firstDay.atTime(0, 0)
        return queries.selectByMonth(start, end)
            .asFlow()
            .mapToList(ioDispatcher)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addExpense(expense: Expense) {
        withContext(ioDispatcher) {
            queries.insert(
                id = expense.id,
                amount = expense.amount,
                merchant = expense.merchant,
                category = expense.category,
                date = expense.date,
                note = expense.note,
                source = expense.source,
                receiptImagePath = expense.receiptImagePath
            )
        }
    }

    override suspend fun getExpenseById(id: String): Expense? = withContext(ioDispatcher) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun updateExpense(expense: Expense) {
        withContext(ioDispatcher) {
            queries.update(
                amount = expense.amount,
                merchant = expense.merchant,
                category = expense.category,
                date = expense.date,
                note = expense.note,
                source = expense.source,
                receiptImagePath = expense.receiptImagePath,
                id = expense.id
            )
        }
    }

    override suspend fun deleteExpense(id: String) {
        withContext(ioDispatcher) {
            queries.deleteById(id)
        }
    }
}

private fun ExpenseEntity.toDomain() = Expense(
    id = id,
    amount = amount,
    merchant = merchant,
    category = category,
    date = date,
    note = note,
    source = source,
    receiptImagePath = receiptImagePath,
)