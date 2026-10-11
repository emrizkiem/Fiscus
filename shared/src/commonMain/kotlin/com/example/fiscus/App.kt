package com.example.fiscus

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.example.fiscus.presentation.navigation.MainScreen
import com.example.fiscus.presentation.common.theme.FiscusTheme

@Composable
fun App() {
    FiscusTheme {
        Navigator(MainScreen())
    }
}
