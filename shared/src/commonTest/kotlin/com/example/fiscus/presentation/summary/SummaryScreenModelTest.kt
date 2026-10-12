package com.example.fiscus.presentation.summary

import com.example.fiscus.domain.model.Category
import com.example.fiscus.domain.model.CategoryTotal
import com.example.fiscus.domain.model.Expense
import com.example.fiscus.domain.model.ExpenseSource
import com.example.fiscus.mock.MockExpenseRepository
import com.example.fiscus.presentation.common.util.SelectedMonthStore
import com.example.fiscus.presentation.expenselist.ExpenseListScreenModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryScreenModelTest {
    private val october = YearMonth(2026, 10)

    private val repository = MockExpenseRepository(
        listOf(
            expense("coffee", 25_000, Category.FOOD, LocalDateTime(2026, 10, 8, 9, 0)),
            expense("groceries", 62_500, Category.SHOPPING, LocalDateTime(2026, 10, 7, 19, 0)),
            expense("bakso", 28_000, Category.FOOD, LocalDateTime(2026, 10, 6, 12, 0)),
            expense("grab", 32_000, Category.TRANSPORT, LocalDateTime(2026, 9, 30, 8, 0)),
            expense("token", 100_000, Category.BILLS, LocalDateTime(2026, 6, 2, 8, 0))
        ),
    )
    private val monthStore = SelectedMonthStore(october)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.createScreenModel(): SummaryScreenModel {
        val screenModel = SummaryScreenModel(repository, currentMonth = october, monthStore = monthStore)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { screenModel.state.collect {} }
        return screenModel
    }

    @Test
    fun showsMonthTotalAndCategoriesLargestFirst() = runTest {
        val state = createScreenModel().state.value

        assertFalse(state.isLoading)
        assertEquals(115_500, state.monthTotal)
        assertEquals(
            listOf(CategoryTotal(Category.SHOPPING, 62_500), CategoryTotal(Category.FOOD, 53_000)),
            state.categoryTotals
        )
    }

    @Test
    fun trendShowsFiveMonthsEndingAtSelectedMonth() = runTest {
        val state = createScreenModel().state.value

        assertEquals(
            listOf(YearMonth(2026, 6), YearMonth(2026, 7), YearMonth(2026, 8), YearMonth(2026, 9), october),
            state.monthlyTotals.map { it.month },
        )
        assertEquals(listOf(100_000L, 0L, 0L, 32_000L, 115_500L), state.monthlyTotals.map { it.total })
    }

    @Test
    fun selectingMonthMovesTrendAndCategories() = runTest {
        val screenModel = createScreenModel()

        screenModel.selectMonth(YearMonth(2026, 9))
        val state = screenModel.state.value

        assertEquals(YearMonth(2026, 9), state.selectedMonth)
        assertEquals(
            listOf(YearMonth(2026, 5), YearMonth(2026, 6), YearMonth(2026, 7), YearMonth(2026, 8), YearMonth(2026, 9)),
            state.monthlyTotals.map { it.month }
        )
        assertEquals(listOf(CategoryTotal(Category.TRANSPORT, 32_000)), state.categoryTotals)
        assertEquals(october, state.currentMonth)
    }

    @Test
    fun monthWithoutExpensesIsMonthEmpty() = runTest {
        val screenModel = createScreenModel()

        screenModel.selectMonth(YearMonth(2026, 8))
        val state = screenModel.state.value

        assertTrue(state.isMonthEmpty)
        assertEquals(0, state.monthTotal)
        assertEquals(5, state.monthlyTotals.size)
    }

    @Test
    fun monthSelectedInTransactionsIsSharedWithSummary() = runTest {
        val summary = createScreenModel()
        val expenseList = ExpenseListScreenModel(repository, currentMonth = october, monthStore = monthStore)

        expenseList.selectMonth(YearMonth(2026, 9))

        assertEquals(YearMonth(2026, 9), summary.state.value.selectedMonth)
    }

    @Test
    fun summaryUpdatesWhenExpenseIsAdded() = runTest {
        val screenModel = createScreenModel()

        repository.addExpense(expense("apotek", 48_000, Category.HEALTH, LocalDateTime(2026, 10, 9, 15, 0)))
        val state = screenModel.state.value

        assertEquals(163_500, state.monthTotal)
        assertEquals(Category.HEALTH, state.categoryTotals.last().category)
    }

    private fun expense(id: String, amount: Long, category: Category, date: LocalDateTime) = Expense(
        id = id,
        amount = amount,
        merchant = null,
        category = category,
        date = date,
        note = null,
        source = ExpenseSource.MANUAL,
        receiptImagePath = null
    )
}
