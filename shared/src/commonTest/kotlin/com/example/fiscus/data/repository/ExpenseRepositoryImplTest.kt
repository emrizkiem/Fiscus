package com.example.fiscus.data.repository

import app.cash.sqldelight.db.SqlDriver
import com.example.fiscus.data.local.createFiscusDatabase
import com.example.fiscus.data.local.createInMemoryDriver
import com.example.fiscus.domain.model.Category
import com.example.fiscus.domain.model.Expense
import com.example.fiscus.domain.model.ExpenseSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExpenseRepositoryImplTest {
    private lateinit var driver: SqlDriver

    @BeforeTest
    fun setUp() {
        driver = createInMemoryDriver()
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    private fun TestScope.createRepository() = ExpenseRepositoryImpl(
        database = createFiscusDatabase(driver),
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun returnsEmptyListForMonthWithoutExpenses() = runTest {
        val repository = createRepository()

        val result = repository.getExpensesByMonth(YearMonth(2026, 10)).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun savedExpenseKeepsAllFields() = runTest {
        val repository = createRepository()
        val saved = Expense(
            id = "a",
            amount = 62_500,
            merchant = "Indomaret",
            category = Category.SHOPPING,
            date = LocalDateTime(2026, 10, 7, 19, 45, 30),
            note = "Belanja mingguan",
            source = ExpenseSource.OCR_GALLERY,
            receiptImagePath = "/receipts/a.jpg",
        )
        repository.addExpense(saved)

        val result = repository.getExpensesByMonth(YearMonth(2026, 10)).first()

        assertEquals(listOf(saved), result)
    }

    @Test
    fun savedExpenseKeepsNullFields() = runTest {
        val repository = createRepository()
        val saved = expense(id = "a", date = LocalDateTime(2026, 10, 7, 12, 0))
        repository.addExpense(saved)

        val result = repository.getExpensesByMonth(YearMonth(2026, 10)).first()

        assertEquals(listOf(saved), result)
    }

    @Test
    fun returnsOnlyExpensesInRequestedMonth() = runTest {
        val repository = createRepository()
        repository.addExpense(expense(id = "sep", date = LocalDateTime(2026, 9, 15, 12, 0)))
        repository.addExpense(expense(id = "oct", date = LocalDateTime(2026, 10, 15, 12, 0)))
        repository.addExpense(expense(id = "nov", date = LocalDateTime(2026, 11, 15, 12, 0)))

        val result = repository.getExpensesByMonth(YearMonth(2026, 10)).first()

        assertEquals(listOf("oct"), result.map { it.id })
    }

    @Test
    fun monthBoundariesAreStartInclusiveEndExclusive() = runTest {
        val repository = createRepository()
        repository.addExpense(expense(id = "last-sep", date = LocalDateTime(2026, 9, 30, 23, 59, 59)))
        repository.addExpense(expense(id = "first-oct", date = LocalDateTime(2026, 10, 1, 0, 0, 0)))
        repository.addExpense(expense(id = "last-oct", date = LocalDateTime(2026, 10, 31, 23, 59, 59)))
        repository.addExpense(expense(id = "first-nov", date = LocalDateTime(2026, 11, 1, 0, 0, 0)))

        val result = repository.getExpensesByMonth(YearMonth(2026, 10)).first()

        assertEquals(setOf("first-oct", "last-oct"), result.map { it.id }.toSet())
    }

    @Test
    fun decemberDoesNotIncludeJanuaryOfNextYear() = runTest {
        val repository = createRepository()
        repository.addExpense(expense(id = "dec", date = LocalDateTime(2026, 12, 31, 23, 59, 59)))
        repository.addExpense(expense(id = "jan", date = LocalDateTime(2027, 1, 1, 0, 0, 0)))

        val december = repository.getExpensesByMonth(YearMonth(2026, 12)).first()
        val january = repository.getExpensesByMonth(YearMonth(2027, 1)).first()

        assertEquals(listOf("dec"), december.map { it.id })
        assertEquals(listOf("jan"), january.map { it.id })
    }

    @Test
    fun expensesAreSortedNewestFirst() = runTest {
        val repository = createRepository()
        repository.addExpense(expense(id = "6-okt", date = LocalDateTime(2026, 10, 6, 9, 0)))
        repository.addExpense(expense(id = "8-okt", date = LocalDateTime(2026, 10, 8, 9, 0)))
        repository.addExpense(expense(id = "7-okt-sore", date = LocalDateTime(2026, 10, 7, 17, 0)))
        repository.addExpense(expense(id = "7-okt-pagi", date = LocalDateTime(2026, 10, 7, 8, 0)))

        val result = repository.getExpensesByMonth(YearMonth(2026, 10)).first()

        assertEquals(
            listOf("8-okt", "7-okt-sore", "7-okt-pagi", "6-okt"),
            result.map { it.id },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun flowEmitsAgainAfterExpenseIsAdded() = runTest {
        val repository = createRepository()
        val emissions = mutableListOf<List<Expense>>()
        backgroundScope.launch {
            repository.getExpensesByMonth(YearMonth(2026, 10)).collect { emissions += it }
        }
        runCurrent()

        repository.addExpense(expense(id = "a", date = LocalDateTime(2026, 10, 8, 9, 0)))
        runCurrent()

        assertEquals(listOf(0, 1), emissions.map { it.size })
    }

    @Test
    fun getExpenseByIdReturnsSavedExpense() = runTest {
        val repository = createRepository()
        val saved = expense(id = "a", date = LocalDateTime(2026, 10, 8, 9, 0))
        repository.addExpense(saved)

        assertEquals(saved, repository.getExpenseById("a"))
    }

    @Test
    fun getExpenseByIdReturnsNullWhenMissing() = runTest {
        val repository = createRepository()

        assertNull(repository.getExpenseById("missing"))
    }

    @Test
    fun updateExpenseReplacesAllFieldsExceptId() = runTest {
        val repository = createRepository()
        repository.addExpense(expense(id = "a", date = LocalDateTime(2026, 10, 8, 9, 0)))
        val updated = Expense(
            id = "a",
            amount = 75_000,
            merchant = "Bakso Pak Eko",
            category = Category.FOOD,
            date = LocalDateTime(2026, 10, 5, 12, 30),
            note = "Makan siang",
            source = ExpenseSource.MANUAL,
            receiptImagePath = null,
        )

        repository.updateExpense(updated)

        assertEquals(listOf(updated), repository.getExpensesByMonth(YearMonth(2026, 10)).first())
    }

    @Test
    fun updateExpenseCanMoveExpenseToAnotherMonth() = runTest {
        val repository = createRepository()
        val original = expense(id = "a", date = LocalDateTime(2026, 10, 8, 9, 0))
        repository.addExpense(original)

        repository.updateExpense(original.copy(date = LocalDateTime(2026, 9, 30, 9, 0)))

        assertTrue(repository.getExpensesByMonth(YearMonth(2026, 10)).first().isEmpty())
        assertEquals(listOf("a"), repository.getExpensesByMonth(YearMonth(2026, 9)).first().map { it.id })
    }

    @Test
    fun deleteExpenseRemovesOnlyThatExpense() = runTest {
        val repository = createRepository()
        repository.addExpense(expense(id = "a", date = LocalDateTime(2026, 10, 8, 9, 0)))
        repository.addExpense(expense(id = "b", date = LocalDateTime(2026, 10, 7, 9, 0)))

        repository.deleteExpense("a")

        assertEquals(listOf("b"), repository.getExpensesByMonth(YearMonth(2026, 10)).first().map { it.id })
        assertNull(repository.getExpenseById("a"))
    }

    @Test
    fun deletedExpenseCanBeRestoredWithSameId() = runTest {
        val repository = createRepository()
        val original = expense(id = "a", date = LocalDateTime(2026, 10, 8, 9, 0))
        repository.addExpense(original)

        repository.deleteExpense("a")
        repository.addExpense(original)

        assertEquals(original, repository.getExpenseById("a"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun flowEmitsAgainAfterExpenseIsDeleted() = runTest {
        val repository = createRepository()
        repository.addExpense(expense(id = "a", date = LocalDateTime(2026, 10, 8, 9, 0)))
        val emissions = mutableListOf<List<Expense>>()
        backgroundScope.launch {
            repository.getExpensesByMonth(YearMonth(2026, 10)).collect { emissions += it }
        }
        runCurrent()

        repository.deleteExpense("a")
        runCurrent()

        assertEquals(listOf(1, 0), emissions.map { it.size })
    }

    private fun expense(id: String, date: LocalDateTime) = Expense(
        id = id,
        amount = 10_000,
        merchant = null,
        category = Category.OTHER,
        date = date,
        note = null,
        source = ExpenseSource.MANUAL,
        receiptImagePath = null,
    )
}
