package com.example.fiscus.presentation.expenselist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.example.fiscus.domain.model.Category
import com.example.fiscus.domain.model.Expense
import com.example.fiscus.presentation.addexpense.AddExpenseScreen
import com.example.fiscus.presentation.common.component.CategoryChip
import com.example.fiscus.presentation.common.component.CategoryIcon
import com.example.fiscus.presentation.common.component.MonthPickerSheet
import com.example.fiscus.presentation.common.util.SnackbarMessage
import com.example.fiscus.presentation.common.util.SnackbarMessenger
import com.example.fiscus.presentation.common.util.formatMonthYear
import com.example.fiscus.presentation.common.util.formatRupiah
import com.example.fiscus.presentation.common.util.formatShortDate
import com.example.fiscus.presentation.common.util.label
import fiscus.shared.generated.resources.Res
import fiscus.shared.generated.resources.ic_banner
import fiscus.shared.generated.resources.ic_calendar
import fiscus.shared.generated.resources.ic_delete
import fiscus.shared.generated.resources.ic_empty_state
import fiscus.shared.generated.resources.ic_logo
import fiscus.shared.generated.resources.ic_receipt
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

object ExpenseListTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Transaksi",
            icon = painterResource(Res.drawable.ic_receipt)
        )

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ExpenseListScreenModel>()
        val rootNavigator = generateSequence(LocalNavigator.currentOrThrow) { it.parent }.last()
        val snackbarMessenger = koinInject<SnackbarMessenger>()
        val state by screenModel.state.collectAsState()
        var showMonthPicker by rememberSaveable { mutableStateOf(false) }

        ExpenseListContent(
            state = state,
            onCategorySelected = screenModel::selectCategory,
            onCalendarClick = { showMonthPicker = true },
            onExpenseClick = { expense -> rootNavigator.push(AddExpenseScreen(expense.id)) },
            onDeleteExpense = { expense ->
                screenModel.deleteExpense(expense)
                snackbarMessenger.show(
                    SnackbarMessage(
                        text = "Pengeluaran dihapus",
                        actionLabel = "Urungkan",
                        onAction = { screenModel.restoreExpense(expense) }
                    )
                )
            }
        )

        if (showMonthPicker) {
            MonthPickerSheet(
                selectedMonth = state.selectedMonth,
                currentMonth = state.currentMonth,
                onMonthSelected = { month ->
                    screenModel.selectMonth(month)
                    showMonthPicker = false
                },
                onDismiss = { showMonthPicker = false }
            )
        }
    }
}

@Composable
private fun ExpenseListContent(
    state: ExpenseListState,
    onCategorySelected: (Category?) -> Unit,
    onCalendarClick: () -> Unit,
    onExpenseClick: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Header(onCalendarClick = onCalendarClick) }
        item { SummaryCard(state) }
        item { CategoryFilterRow(state.selectedCategory, onCategorySelected) }
        when {
            state.isMonthEmpty -> item {
                EmptyMessage("Belum ada pengeluaran di ${formatMonthYear(state.selectedMonth)}")
            }
            state.isCategoryEmpty -> item {
                EmptyMessage("Belum ada pengeluaran ${state.selectedCategory?.label} di ${formatMonthYear(state.selectedMonth)}")
            }
            else -> items(state.expenses, key = { it.id }) { expense ->
                SwipeToDeleteItem(
                    onDelete = { onDeleteExpense(expense) },
                    modifier = Modifier.animateItem()
                ) {
                    ExpenseItem(expense, onClick = { onExpenseClick(expense) })
                }
            }
        }
    }
}

@Composable
private fun Header(onCalendarClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fiscus", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(6.dp))
            Image(
                painter = painterResource(Res.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = onCalendarClick
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_calendar),
                    contentDescription = "Pilih bulan",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(state: ExpenseListState) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        append("Total Pengeluaran ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(formatMonthYear(state.selectedMonth))
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    formatRupiah(state.monthTotal),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text("${state.monthTransactionCount} transaksi", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(8.dp))
            Image(
                painter = painterResource(Res.drawable.ic_banner),
                contentDescription = null,
                modifier = Modifier.height(88.dp).aspectRatio(742.85f / 800f)
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(selected: Category?, onSelected: (Category?) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        item { CategoryChip("Semua", selected == null) { onSelected(null) } }
        items(Category.entries) { category ->
            CategoryChip(category.label, selected == category) { onSelected(category) }
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(expense.category)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    expense.merchant ?: expense.category.label,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatShortDate(expense.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(formatRupiah(expense.amount), style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_empty_state),
            contentDescription = null,
            modifier = Modifier.height(150.dp).aspectRatio(822f / 694.94f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val dismissState = remember { SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled, positionalThreshold) }
    val currentOnDelete by rememberUpdatedState(onDelete)
    val handleDismiss: (SwipeToDismissBoxValue) -> Unit = remember {
        { direction -> if (direction == SwipeToDismissBoxValue.EndToStart) currentOnDelete() }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        onDismiss = handleDismiss,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painterResource(Res.drawable.ic_delete),
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        content()
    }
}
