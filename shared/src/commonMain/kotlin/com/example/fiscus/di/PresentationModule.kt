package com.example.fiscus.di

import com.example.fiscus.presentation.addexpense.AddExpenseScreenModel
import com.example.fiscus.presentation.common.util.ExpenseAddedEvents
import com.example.fiscus.presentation.common.util.PendingReceipt
import com.example.fiscus.presentation.common.util.SelectedMonthStore
import com.example.fiscus.presentation.common.util.SnackbarMessenger
import com.example.fiscus.presentation.expenselist.ExpenseListScreenModel
import com.example.fiscus.presentation.summary.SummaryScreenModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import org.koin.dsl.module
import kotlin.time.Clock

val presentationModule = module {
    single { SnackbarMessenger() }
    single { ExpenseAddedEvents() }
    single { SelectedMonthStore(initialMonth = currentYearMonth()) }
    single { PendingReceipt() }
    factory { ExpenseListScreenModel(repository = get(), currentMonth = currentYearMonth(), monthStore = get()) }
    factory { params ->
        AddExpenseScreenModel(
            repository = get(),
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            receiptReader = get(),
            expenseId = params.getOrNull<String>(),
            receiptImage = if (params.getOrNull<Boolean>() == true) get<PendingReceipt>().take() else null,
        )
    }
    factory { SummaryScreenModel(repository = get(), currentMonth = currentYearMonth(), monthStore = get()) }
}

private fun currentYearMonth(): YearMonth {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return YearMonth(today.year, today.month)
}
