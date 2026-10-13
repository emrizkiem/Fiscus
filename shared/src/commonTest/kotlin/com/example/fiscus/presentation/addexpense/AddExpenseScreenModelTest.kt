package com.example.fiscus.presentation.addexpense

import com.example.fiscus.domain.model.Category
import com.example.fiscus.domain.model.Expense
import com.example.fiscus.domain.model.ExpenseSource
import com.example.fiscus.domain.model.ReceiptScanResult
import com.example.fiscus.mock.MockExpenseRepository
import com.example.fiscus.mock.MockReceiptReader
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

    private fun createScreenModel(
        expenseId: String? = null,
        receiptImage: ByteArray? = null,
        receiptReader: MockReceiptReader = MockReceiptReader(),
    ) = AddExpenseScreenModel(
        repository = repository,
        clock = fixedClock,
        timeZone = TimeZone.UTC,
        receiptReader = receiptReader,
        expenseId = expenseId,
        receiptImage = receiptImage,
    )

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

    private val existing = Expense(
        id = "existing",
        amount = 62_500,
        merchant = "Indomaret",
        category = Category.SHOPPING,
        date = LocalDateTime(2026, 10, 7, 19, 45, 0),
        note = null,
        source = ExpenseSource.OCR_GALLERY,
        receiptImagePath = "/receipts/a.jpg",
    )

    @Test
    fun editModeLoadsExistingExpenseIntoForm() = runTest {
        repository.addExpense(existing)

        val state = createScreenModel(expenseId = "existing").state.value

        assertTrue(state.isEditMode)
        assertEquals("62500", state.amountDigits)
        assertEquals("Indomaret", state.merchant)
        assertEquals(Category.SHOPPING, state.category)
        assertEquals(LocalDate(2026, 10, 7), state.date)
        assertEquals("", state.note)
    }

    @Test
    fun editSaveUpdatesInsteadOfAddingAndKeepsUneditedFields() = runTest {
        repository.addExpense(existing)
        val screenModel = createScreenModel(expenseId = "existing")

        screenModel.onAmountChange("70000")
        screenModel.onCategorySelected(Category.FOOD)
        screenModel.onDateSelected(LocalDate(2026, 10, 5))
        screenModel.save()

        val saved = savedExpenses().single()
        assertEquals("existing", saved.id)
        assertEquals(70_000, saved.amount)
        assertEquals(Category.FOOD, saved.category)
        assertEquals(LocalDateTime(2026, 10, 5, 19, 45, 0), saved.date)
        assertEquals(ExpenseSource.OCR_GALLERY, saved.source)
        assertEquals("/receipts/a.jpg", saved.receiptImagePath)
        assertTrue(screenModel.state.value.isSaved)
    }

    @Test
    fun editModeForMissingExpenseDoesNotSave() = runTest {
        val screenModel = createScreenModel(expenseId = "missing")

        screenModel.onAmountChange("10000")
        screenModel.onCategorySelected(Category.OTHER)
        screenModel.save()

        assertTrue(savedExpenses().isEmpty())
        assertFalse(screenModel.state.value.isSaved)
    }

    private val receiptBytes = byteArrayOf(1, 2, 3)

    @Test
    fun reviewModeFillsFieldsFromReceipt() = runTest {
        val reader = MockReceiptReader(ReceiptScanResult(amount = 127_600, merchant = "Alfamart", date = LocalDate(2026, 9, 18)))

        val state = createScreenModel(receiptImage = receiptBytes, receiptReader = reader).state.value

        assertTrue(state.isReviewMode)
        assertFalse(state.isReadingReceipt)
        assertEquals("127600", state.amountDigits)
        assertEquals("Alfamart", state.merchant)
        assertEquals(LocalDate(2026, 9, 18), state.date)
        assertNull(state.category)
        assertEquals(1, reader.readCount)
    }

    @Test
    fun reviewModeLeavesUnreadFieldsEmptyAndDateToday() = runTest {
        val state = createScreenModel(receiptImage = receiptBytes).state.value

        assertEquals("", state.amountDigits)
        assertEquals("", state.merchant)
        assertEquals(LocalDate(2026, 10, 9), state.date)
    }

    @Test
    fun reviewModeClampsFutureReceiptDateToToday() = runTest {
        val reader = MockReceiptReader(ReceiptScanResult(amount = 10_000, merchant = null, date = LocalDate(2026, 12, 1)))

        val state = createScreenModel(receiptImage = receiptBytes, receiptReader = reader).state.value

        assertEquals(LocalDate(2026, 10, 9), state.date)
    }

    @Test
    fun reviewModeSurvivesOcrFailure() = runTest {
        val state = createScreenModel(receiptImage = receiptBytes, receiptReader = MockReceiptReader(result = null)).state.value

        assertFalse(state.isReadingReceipt)
        assertEquals("", state.amountDigits)
    }

    @Test
    fun savingReviewMarksSourceAsGalleryWithoutKeepingImage() = runTest {
        val reader = MockReceiptReader(ReceiptScanResult(amount = 127_600, merchant = "Alfamart", date = LocalDate(2026, 9, 18)))
        val screenModel = createScreenModel(receiptImage = receiptBytes, receiptReader = reader)

        screenModel.onCategorySelected(Category.SHOPPING)
        screenModel.save()

        val saved = repository.getExpensesByMonth(YearMonth(2026, 9)).first().single()
        assertEquals(127_600, saved.amount)
        assertEquals(ExpenseSource.OCR_GALLERY, saved.source)
        assertNull(saved.receiptImagePath)
    }

    @Test
    fun manualSaveIsMarkedAsManual() = runTest {
        val screenModel = createScreenModel()
        screenModel.onAmountChange("15000")
        screenModel.onCategorySelected(Category.OTHER)

        screenModel.save()

        assertNull(savedExpenses().single().receiptImagePath)
        assertEquals(ExpenseSource.MANUAL, savedExpenses().single().source)
    }
}
