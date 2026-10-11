package com.example.fiscus.di

import app.cash.sqldelight.db.SqlDriver
import com.example.fiscus.data.local.DatabaseDriverFactory
import org.koin.dsl.module

fun initKoinIos() {
    initKoin(
        module {
            single<SqlDriver> { DatabaseDriverFactory().createDriver() }
        },
    )
}
