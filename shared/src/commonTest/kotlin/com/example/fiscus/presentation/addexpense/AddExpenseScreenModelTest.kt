package com.example.fiscus.presentation.addexpense

import com.example.fiscus.domain.model.Category
import com.example.fiscus.domain.model.ExpenseSource
import com.example.fiscus.mock.MockExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toInstant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseScreenModelTest {
    private val now = LocalDateTime(2026, 10, 9, 14, 30, 15)
    private val fixedClock = object : Clock {
        override fun now(): Instant = this@AddExpenseScreenModelTest.now.toInstant(TimeZone.UTC)
    }
    private val repository = MockExpenseRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createScreenModel() = AddExpenseScreenModel(repository, fixedClock, TimeZone.UTC)

    private suspend fun savedExpenses() = repository.getExpensesByMonth(YearMonth(2026, 10)).first()

    @Test
    fun startsEmptyWithTodayAsDate() {
        val state = createScreenModel().state.value

        assertEquals(LocalDate(2026, 10, 9), state.date)
        assertEquals("", state.amountDigits)
        assertNull(state.category)
    }

    @Test
    fun amountKeepsDigitsOnlyWithoutLeadingZeros() {
        val screenModel = createScreenModel()

        screenModel.onAmountChange("0062.500")

        assertEquals("62500", screenModel.state.value.amountDigits)
    }

    @Test
    fun amountIsLimitedToTwelveDigits() {
        val screenModel = createScreenModel()

        screenModel.onAmountChange("1234567890123456")

        assertEquals("123456789012", screenModel.state.value.amountDigits)
    }

    @Test
    fun saveWithoutAmountAndCategoryShowsBothErrors() = runTest {
        val screenModel = createScreenModel()

        screenModel.save()
        val state = screenModel.state.value

        assertTrue(state.showAmountError)
        assertTrue(state.showCategoryError)
        assertFalse(state.isSaved)
        assertTrue(savedExpenses().isEmpty())
    }

    @Test
    fun fixingFieldsClearsTheirErrors() {
        val screenModel = createScreenModel()
        screenModel.save()

        screenModel.onAmountChange("25000")
        screenModel.onCategorySelected(Category.FOOD)
        val state = screenModel.state.value

        assertFalse(state.showAmountError)
        assertFalse(state.showCategoryError)
    }

    @Test
    fun futureDateIsClampedToToday() {
        val screenModel = createScreenModel()

        screenModel.onDateSelected(LocalDate(2026, 12, 1))

        assertEquals(LocalDate(2026, 10, 9), screenModel.state.value.date)
    }

    @Test
    fun saveStoresManualExpenseWithSelectedDateAndCurrentTime() = runTest {
        val screenModel = createScreenModel()
        screenModel.onAmountChange("62500")
        screenModel.onMerchantChange("  Indomaret ")
        screenModel.onCategorySelected(Category.SHOPPING)
        screenModel.onDateSelected(LocalDate(2026, 10, 7))
        screenModel.onNoteChange("Belanja mingguan")

        screenModel.save()

        val saved = savedExpenses().single()
        assertEquals(62_500, saved.amount)
        assertEquals("Indomaret", saved.merchant)
        assertEquals(Category.SHOPPING, saved.category)
        assertEquals(LocalDateTime(2026, 10, 7, 14, 30, 15), saved.date)
        assertEquals("Belanja mingguan", saved.note)
        assertEquals(ExpenseSource.MANUAL, saved.source)
        assertTrue(screenModel.state.value.isSaved)
    }

    @Test
    fun blankMerchantAndNoteAreSavedAsNull() = runTest {
        val screenModel = createScreenModel()
        screenModel.onAmountChange("15000")
        screenModel.onMerchantChange("   ")
        screenModel.onCategorySelected(Category.OTHER)
        screenModel.onNoteChange("")

        screenModel.save()

        val saved = savedExpenses().single()
        assertNull(saved.merchant)
        assertNull(saved.note)
    }

    @Test
    fun savingTwiceStoresOnlyOneExpense() = runTest {
        val screenModel = createScreenModel()
        screenModel.onAmountChange("15000")
        screenModel.onCategorySelected(Category.OTHER)

        screenModel.save()
        screenModel.save()

        assertEquals(1, savedExpenses().size)
    }
}
