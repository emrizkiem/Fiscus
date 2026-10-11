package com.example.fiscus.di

import com.example.fiscus.presentation.addexpense.AddExpenseScreenModel
import com.example.fiscus.presentation.common.util.SnackbarMessenger
import com.example.fiscus.presentation.expenselist.ExpenseListScreenModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import org.koin.dsl.module
import kotlin.time.Clock

val presentationModule = module {
    single { SnackbarMessenger() }
    factory { ExpenseListScreenModel(repository = get(), currentMonth = currentYearMonth()) }
    factory {
        AddExpenseScreenModel(repository = get(), clock = Clock.System, timeZone = TimeZone.currentSystemDefault())
    }
}

private fun currentYearMonth(): YearMonth {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return YearMonth(today.year, today.month)
}
