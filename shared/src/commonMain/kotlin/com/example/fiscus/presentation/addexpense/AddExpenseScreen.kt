package com.example.fiscus.presentation.addexpense

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.fiscus.presentation.common.util.SnackbarMessenger
import org.koin.compose.koinInject

class AddExpenseScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<AddExpenseScreenModel>()
        val snackbarMessenger = koinInject<SnackbarMessenger>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.isSaved) {
            if (state.isSaved) {
                snackbarMessenger.show("Pengeluaran disimpan")
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
            onSave = screenModel::save,
        )
    }
}
