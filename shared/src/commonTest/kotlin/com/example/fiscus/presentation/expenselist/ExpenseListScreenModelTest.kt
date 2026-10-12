package com.example.fiscus.presentation.expenselist

import com.example.fiscus.domain.model.Category
import com.example.fiscus.domain.model.Expense
import com.example.fiscus.domain.model.ExpenseSource
import com.example.fiscus.mock.MockExpenseRepository
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
class ExpenseListScreenModelTest {
    private val october = YearMonth(2026, 10)
    private val september = YearMonth(2026, 9)

    private val coffee = expense("coffee", 25_000, Category.FOOD, LocalDateTime(2026, 10, 8, 9, 0))
    private val groceries = expense("groceries", 62_500, Category.SHOPPING, LocalDateTime(2026, 10, 7, 19, 0))
    private val bakso = expense("bakso", 28_000, Category.FOOD, LocalDateTime(2026, 10, 6, 12, 0))
    private val grab = expense("grab", 32_000, Category.TRANSPORT, LocalDateTime(2026, 9, 30, 8, 0))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.createScreenModel(
        repository: MockExpenseRepository = MockExpenseRepository(listOf(coffee, groceries, bakso, grab)),
    ): ExpenseListScreenModel {
        val screenModel = ExpenseListScreenModel(repository, currentMonth = october)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { screenModel.state.collect {} }
        return screenModel
    }

    @Test
    fun showsTotalAndCountOfCurrentMonth() = runTest {
        val state = createScreenModel().state.value

        assertFalse(state.isLoading)
        assertEquals(october, state.selectedMonth)
        assertEquals(115_500, state.monthTotal)
        assertEquals(3, state.monthTransactionCount)
        assertEquals(listOf("coffee", "groceries", "bakso"), state.expenses.map { it.id })
    }

    @Test
    fun categoryFilterChangesListButNotMonthTotal() = runTest {
        val screenModel = createScreenModel()

        screenModel.selectCategory(Category.FOOD)
        val state = screenModel.state.value

        assertEquals(listOf("coffee", "bakso"), state.expenses.map { it.id })
        assertEquals(115_500, state.monthTotal)
        assertEquals(3, state.monthTransactionCount)
    }

    @Test
    fun selectingAllShowsEveryExpenseAgain() = runTest {
        val screenModel = createScreenModel()

        screenModel.selectCategory(Category.FOOD)
        screenModel.selectCategory(null)

        assertEquals(3, screenModel.state.value.expenses.size)
    }

    @Test
    fun categoryWithoutExpensesIsCategoryEmpty() = runTest {
        val screenModel = createScreenModel()

        screenModel.selectCategory(Category.HEALTH)
        val state = screenModel.state.value

        assertTrue(state.isCategoryEmpty)
        assertFalse(state.isMonthEmpty)
    }

    @Test
    fun monthWithoutExpensesIsMonthEmpty() = runTest {
        val screenModel = createScreenModel()

        screenModel.selectMonth(YearMonth(2026, 8))
        val state = screenModel.state.value

        assertTrue(state.isMonthEmpty)
        assertFalse(state.isCategoryEmpty)
        assertEquals(0, state.monthTotal)
    }

    @Test
    fun selectingMonthShowsThatMonthAndKeepsCategory() = runTest {
        val screenModel = createScreenModel()

        screenModel.selectCategory(Category.TRANSPORT)
        screenModel.selectMonth(september)
        val state = screenModel.state.value

        assertEquals(september, state.selectedMonth)
        assertEquals(october, state.currentMonth)
        assertEquals(Category.TRANSPORT, state.selectedCategory)
        assertEquals(listOf("grab"), state.expenses.map { it.id })
        assertEquals(32_000, state.monthTotal)
    }

    @Test
    fun stateUpdatesWhenExpenseIsAdded() = runTest {
        val repository = MockExpenseRepository(listOf(coffee))
        val screenModel = createScreenModel(repository)

        repository.addExpense(groceries)
        val state = screenModel.state.value

        assertEquals(87_500, state.monthTotal)
        assertEquals(listOf("coffee", "groceries"), state.expenses.map { it.id })
    }

    @Test
    fun deletingExpenseRemovesItAndUpdatesTotal() = runTest {
        val screenModel = createScreenModel()

        screenModel.deleteExpense(groceries)
        val state = screenModel.state.value

        assertEquals(listOf("coffee", "bakso"), state.expenses.map { it.id })
        assertEquals(53_000, state.monthTotal)
        assertEquals(2, state.monthTransactionCount)
    }

    @Test
    fun restoringDeletedExpenseBringsItBack() = runTest {
        val screenModel = createScreenModel()

        screenModel.deleteExpense(groceries)
        screenModel.restoreExpense(groceries)
        val state = screenModel.state.value

        assertEquals(listOf("coffee", "groceries", "bakso"), state.expenses.map { it.id })
        assertEquals(115_500, state.monthTotal)
    }

    private fun expense(id: String, amount: Long, category: Category, date: LocalDateTime) = Expense(
        id = id,
        amount = amount,
        merchant = null,
        category = category,
        date = date,
        note = null,
        source = ExpenseSource.MANUAL,
        receiptImagePath = null,
    )
}
