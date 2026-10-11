package com.example.fiscus.presentation.addexpense

import com.example.fiscus.domain.model.Category
import kotlinx.datetime.LocalDate

data class AddExpenseState(
    val today: LocalDate,
    val date: LocalDate = today,
    val amountDigits: String = "",
    val merchant: String = "",
    val category: Category? = null,
    val note: String = "",
    val showAmountError: Boolean = false,
    val showCategoryError: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)
