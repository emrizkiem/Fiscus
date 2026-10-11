package com.example.fiscus.data.local

import app.cash.sqldelight.db.SqlDriver

expect fun createInMemoryDriver(): SqlDriver
