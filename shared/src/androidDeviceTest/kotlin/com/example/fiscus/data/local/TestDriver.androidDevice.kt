package com.example.fiscus.data.local

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual fun createInMemoryDriver(): SqlDriver = AndroidSqliteDriver(
    schema = FiscusDatabase.Schema,
    context = InstrumentationRegistry.getInstrumentation().targetContext,
    name = null,
)
