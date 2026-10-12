package com.example.fiscus.presentation.addexpense

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.fiscus.domain.model.Category
import com.example.fiscus.domain.model.Expense
import com.example.fiscus.domain.model.ExpenseSource
import com.example.fiscus.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val MAX_AMOUNT_DIGITS = 12

class AddExpenseScreenModel(
    private val repository: ExpenseRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    expenseId: String? = null
) : ScreenModel {
    private val _state = MutableStateFlow(AddExpenseState(today = now().date, isEditMode = expenseId != null))
    val state: StateFlow<AddExpenseState> = _state.asStateFlow()

    private var original: Expense? = null

    init {
        if (expenseId != null) {
            screenModelScope.launch {
                val expense = repository.getExpenseById(expenseId) ?: return@launch
                original = expense
                _state.update {
                    it.copy(
                        amountDigits = expense.amount.toString(),
                        merchant = expense.merchant.orEmpty(),
                        category = expense.category,
                        date = expense.date.date,
                        note = expense.note.orEmpty()
                    )
                }
            }
        }
    }

    fun onAmountChange(input: String) {
        val digits = input.filter { it.isDigit() }.trimStart('0').take(MAX_AMOUNT_DIGITS)
        _state.update { it.copy(amountDigits = digits, showAmountError = false) }
    }

    fun onMerchantChange(merchant: String) {
        _state.update { it.copy(merchant = merchant) }
    }

    fun onCategorySelected(category: Category) {
        _state.update { it.copy(category = category, showCategoryError = false) }
    }

    fun onDateSelected(date: LocalDate) {
        _state.update { it.copy(date = minOf(date, it.today)) }
    }

    fun onNoteChange(note: String) {
        _state.update { it.copy(note = note) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun save() {
        val current = _state.value
        if (current.isSaving || current.isSaved) return
        val editing = original
        if (current.isEditMode && editing == null) return

        val amount = current.amountDigits.toLongOrNull() ?: 0L
        val category = current.category
        if (amount <= 0L || category == null) {
            _state.update { it.copy(showAmountError = amount <= 0L, showCategoryError = category == null) }
            return
        }

        _state.update { it.copy(isSaving = true) }
        val merchant = current.merchant.trim().ifEmpty { null }
        val note = current.note.trim().ifEmpty { null }

        screenModelScope.launch {
            if (editing != null) {
                repository.updateExpense(
                    editing.copy(
                        amount = amount,
                        merchant = merchant,
                        category = category,
                        date = LocalDateTime(current.date, editing.date.time),
                        note = note
                    ),
                )
            } else {
                val time = now().time
                repository.addExpense(
                    Expense(
                        id = Uuid.random().toString(),
                        amount = amount,
                        merchant = merchant,
                        category = category,
                        date = LocalDateTime(current.date, LocalTime(time.hour, time.minute, time.second)),
                        note = note,
                        source = ExpenseSource.MANUAL,
                        receiptImagePath = null
                    ),
                )
            }
            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private fun now(): LocalDateTime = clock.now().toLocalDateTime(timeZone)
}
