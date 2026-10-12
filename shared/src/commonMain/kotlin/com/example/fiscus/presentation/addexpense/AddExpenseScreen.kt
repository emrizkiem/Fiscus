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
import com.example.fiscus.presentation.common.util.SnackbarMessenger
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

class AddExpenseScreen(private val expenseId: String? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AddExpenseScreenModel> { parametersOf(expenseId) }
        val snackbarMessenger = koinInject<SnackbarMessenger>()
        val expenseAddedEvents = koinInject<ExpenseAddedEvents>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.isSaved) {
            if (state.isSaved) {
                snackbarMessenger.show(if (state.isEditMode) "Perubahan disimpan" else "Pengeluaran disimpan")
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
