package com.example.fiscus.presentation.addexpense

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.fiscus.presentation.common.util.ExpenseAddedEvents
import com.example.fiscus.presentation.common.util.SelectedMonthStore
import com.example.fiscus.presentation.common.util.SnackbarMessage
import com.example.fiscus.presentation.common.util.SnackbarMessenger
import com.example.fiscus.presentation.common.util.formatMonthYear
import kotlinx.datetime.YearMonth
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

class AddExpenseScreen(
    private val expenseId: String? = null,
    private val isReceiptReview: Boolean = false,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AddExpenseScreenModel> { parametersOf(expenseId, isReceiptReview) }
        val snackbarMessenger = koinInject<SnackbarMessenger>()
        val expenseAddedEvents = koinInject<ExpenseAddedEvents>()
        val monthStore = koinInject<SelectedMonthStore>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.isSaved) {
            if (state.isSaved) {
                val savedMonth = YearMonth(state.date.year, state.date.month)
                val isOtherMonth = savedMonth != monthStore.selectedMonth.value
                snackbarMessenger.show(
                    when {
                        isOtherMonth -> SnackbarMessage(
                            text = if (state.isEditMode) {
                                "Dipindah ke ${formatMonthYear(savedMonth)}"
                            } else {
                                "Disimpan di ${formatMonthYear(savedMonth)}"
                            },
                            actionLabel = "Lihat",
                            onAction = { monthStore.select(savedMonth) }
                        )
                        state.isEditMode -> SnackbarMessage("Perubahan disimpan")
                        else -> SnackbarMessage("Pengeluaran disimpan")
                    }
                )
                if (!state.isEditMode) expenseAddedEvents.notifyAdded()
                navigator.pop()
            }
        }

        AddExpenseContent(
            state = state,
            onBack = { navigator.pop() },
            onAmountChange = screenModel::onAmountChange,
            onMerchantChange = screenModel::onMerchantChange,
            onCategorySelected = screenModel::onCategorySelected,
            onDateSelected = screenModel::onDateSelected,
            onNoteChange = screenModel::onNoteChange,
            onSave = screenModel::save
        )
    }
}
