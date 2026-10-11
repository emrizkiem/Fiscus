package com.example.fiscus.presentation.expenselist

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.fiscus.domain.model.Category
import com.example.fiscus.domain.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.YearMonth

class ExpenseListScreenModel(
    private val repository: ExpenseRepository,
    private val currentMonth: YearMonth,
) : ScreenModel {
    private val selectedMonth = MutableStateFlow(currentMonth)
    private val selectedCategory = MutableStateFlow<Category?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthExpenses = selectedMonth.flatMapLatest { month ->
        repository.getExpensesByMonth(month).map { expenses -> month to expenses }
    }

    val state: StateFlow<ExpenseListState> =
        combine(monthExpenses, selectedCategory) { (month, expenses), category ->
            ExpenseListState(
                currentMonth = currentMonth,
                selectedMonth = month,
                selectedCategory = category,
                monthTotal = expenses.sumOf { it.amount },
                monthTransactionCount = expenses.size,
                expenses = if (category == null) expenses else expenses.filter { it.category == category },
                isLoading = false,
            )
        }.stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExpenseListState(currentMonth = currentMonth, selectedMonth = currentMonth),
        )

    fun selectMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    fun selectCategory(category: Category?) {
        selectedCategory.value = category
    }
}
