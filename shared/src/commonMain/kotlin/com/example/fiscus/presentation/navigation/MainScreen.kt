package com.example.fiscus.presentation.navigation

import androidx.compose.foundation.layout.Box
import com.example.fiscus.data.ocr.ReceiptScanner
import com.example.fiscus.data.ocr.assembleLines
import com.example.fiscus.data.parser.ReceiptParser
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.example.fiscus.presentation.addexpense.AddExpenseScreen
import com.example.fiscus.presentation.common.component.AddExpenseOption
import com.example.fiscus.presentation.common.component.AddExpenseSheet
import com.example.fiscus.presentation.common.component.FiscusSnackbar
import com.example.fiscus.presentation.common.picker.rememberImagePickerLauncher
import com.example.fiscus.presentation.common.util.ExpenseAddedEvents
import com.example.fiscus.presentation.common.util.PendingReceipt
import com.example.fiscus.presentation.common.util.SnackbarMessenger
import com.example.fiscus.presentation.common.util.formatRupiah
import com.example.fiscus.presentation.common.util.formatFullDate
import com.example.fiscus.presentation.expenselist.ExpenseListTab
import com.example.fiscus.presentation.summary.SummaryTab
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var showAddSheet by rememberSaveable { mutableStateOf(false) }
        val snackbarMessenger = koinInject<SnackbarMessenger>()
        val expenseAddedEvents = koinInject<ExpenseAddedEvents>()
        val snackbarHostState = remember { SnackbarHostState() }
        val pendingReceipt = koinInject<PendingReceipt>()
        val imagePicker = rememberImagePickerLauncher { bytes ->
            if (bytes != null) {
                pendingReceipt.put(bytes)
                navigator.push(AddExpenseScreen(isReceiptReview = true))
            }
        }

        LaunchedEffect(snackbarMessenger) {
            snackbarMessenger.messages.collect { message ->
                snackbarHostState.currentSnackbarData?.dismiss()
                this@LaunchedEffect.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = message.text,
                        actionLabel = message.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) message.onAction?.invoke()
                }
            }
        }

        TabNavigator(ExpenseListTab) { tabNavigator ->
            LaunchedEffect(expenseAddedEvents) {
                expenseAddedEvents.events.collect { tabNavigator.current = ExpenseListTab }
            }
            Scaffold(
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data -> FiscusSnackbar(data) }
                },
                bottomBar = {
                    FiscusBottomBar(
                        leftTab = ExpenseListTab,
                        rightTab = SummaryTab,
                        currentTab = tabNavigator.current,
                        isAddActive = showAddSheet,
                        onTabSelected = { tabNavigator.current = it },
                        onAddClick = { showAddSheet = true }
                    )
                },
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    CurrentTab()
                }
            }
        }

        if (showAddSheet) {
            AddExpenseSheet(
                onDismiss = { showAddSheet = false },
                onOptionSelected = { option ->
                    showAddSheet = false
                    when (option) {
                        AddExpenseOption.MANUAL -> navigator.push(AddExpenseScreen())
                        AddExpenseOption.GALLERY -> imagePicker.launch()
                        AddExpenseOption.CAMERA -> Unit
                    }
                },
            )
        }
    }
}
