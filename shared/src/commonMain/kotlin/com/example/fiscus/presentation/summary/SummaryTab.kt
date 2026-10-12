package com.example.fiscus.presentation.summary

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.example.fiscus.presentation.common.component.MonthPickerSheet
import com.example.fiscus.presentation.common.util.formatMonthYear
import com.example.fiscus.presentation.common.util.formatRupiah
import fiscus.shared.generated.resources.Res
import fiscus.shared.generated.resources.ic_calendar
import fiscus.shared.generated.resources.ic_empty_state
import fiscus.shared.generated.resources.ic_pie_chart
import org.jetbrains.compose.resources.painterResource

object SummaryTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Ringkasan",
            icon = painterResource(Res.drawable.ic_pie_chart),
        )

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SummaryScreenModel>()
        val state by screenModel.state.collectAsState()
        var showMonthPicker by rememberSaveable { mutableStateOf(false) }

        SummaryContent(state = state, onCalendarClick = { showMonthPicker = true })

        if (showMonthPicker) {
            MonthPickerSheet(
                selectedMonth = state.selectedMonth,
                currentMonth = state.currentMonth,
                onMonthSelected = { month ->
                    screenModel.selectMonth(month)
                    showMonthPicker = false
                },
                onDismiss = { showMonthPicker = false },
            )
        }
    }
}

@Composable
private fun SummaryContent(state: SummaryState, onCalendarClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Header(onCalendarClick = onCalendarClick)
        MonthTotalHeader(state)
        Section(title = "Tren 5 bulan") {
            MonthTrendChart(monthlyTotals = state.monthlyTotals, selectedMonth = state.selectedMonth)
        }
        Section(title = "Per kategori") {
            if (state.isMonthEmpty) {
                EmptyCategories(text = "Belum ada pengeluaran di ${formatMonthYear(state.selectedMonth)}")
            } else {
                CategoryBreakdown(categoryTotals = state.categoryTotals)
            }
        }
    }
}

@Composable
private fun Header(onCalendarClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Ringkasan", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        IconButton(onClick = onCalendarClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_calendar),
                contentDescription = "Pilih bulan",
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun MonthTotalHeader(state: SummaryState) {
    Column {
        Text(
            text = buildAnnotatedString {
                append("Total pengeluaran ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(formatMonthYear(state.selectedMonth))
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            formatRupiah(state.monthTotal),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

@Composable
private fun EmptyCategories(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_empty_state),
            contentDescription = null,
            modifier = Modifier.height(120.dp).aspectRatio(822f / 694.94f),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
