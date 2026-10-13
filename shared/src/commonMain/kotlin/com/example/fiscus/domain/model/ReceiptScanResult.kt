package com.example.fiscus.domain.model

import kotlinx.datetime.LocalDate

data class ReceiptScanResult(
    val amount: Long?,
    val merchant: String?,
    val date: LocalDate?
)
