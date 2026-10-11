package com.example.fiscus.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.example.fiscus.presentation.common.component.AddExpenseSheet
import com.example.fiscus.presentation.expenselist.ExpenseListTab
import com.example.fiscus.presentation.summary.SummaryTab

class MainScreen : Screen {
    @Composable
    override fun Content() {
        var showAddSheet by rememberSaveable { mutableStateOf(false) }

        TabNavigator(ExpenseListTab) { tabNavigator ->
            Scaffold(
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
                // TODO: arahkan ke layar kamera/galeri/form setelah layarnya ada
                onOptionSelected = { showAddSheet = false },
            )
        }
    }
}
