package com.example.fiscus.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver

actual fun createInMemoryDriver(): SqlDriver = inMemoryDriver(FiscusDatabase.Schema)
