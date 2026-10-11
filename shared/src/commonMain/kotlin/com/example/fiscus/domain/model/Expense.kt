package com.example.fiscus.domain.model

import kotlinx.datetime.LocalDateTime

data class Expense(
    val id: String,
    val amount: Long,
    val merchant: String?,
    val category: Category,
    val date: LocalDateTime,
    val note: String?,
    val source: ExpenseSource,
    val receiptImagePath: String?,
)
