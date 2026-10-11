package com.example.fiscus.data.local

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver

fun createFiscusDatabase(driver: SqlDriver): FiscusDatabase = FiscusDatabase(
    driver = driver,
    expenseEntityAdapter = ExpenseEntity.Adapter(
        categoryAdapter = EnumColumnAdapter(),
        dateAdapter = LocalDateTimeAdapter,
        sourceAdapter = EnumColumnAdapter(),
    ),
)
