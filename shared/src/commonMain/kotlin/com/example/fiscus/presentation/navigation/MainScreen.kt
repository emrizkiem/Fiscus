package com.example.fiscus.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.fiscus.presentation.common.util.SnackbarMessenger
import com.example.fiscus.presentation.expenselist.ExpenseListTab
import com.example.fiscus.presentation.summary.SummaryTab
import org.koin.compose.koinInject

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var showAddSheet by rememberSaveable { mutableStateOf(false) }
        val snackbarMessenger = koinInject<SnackbarMessenger>()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(snackbarMessenger) {
            snackbarMessenger.messages.collect { snackbarHostState.showSnackbar(it) }
        }

        TabNavigator(ExpenseListTab) { tabNavigator ->
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
                        onAddClick = { showAddSheet = true },
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
                        // TODO: alur kamera/galeri (OCR)
                        AddExpenseOption.CAMERA, AddExpenseOption.GALLERY -> Unit
                    }
                },
            )
        }
    }
}
