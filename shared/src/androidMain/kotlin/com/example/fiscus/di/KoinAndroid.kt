package com.example.fiscus.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import com.example.fiscus.data.local.DatabaseDriverFactory
import org.koin.dsl.module

fun initKoinAndroid(context: Context) {
    val appContext = context.applicationContext
    initKoin(
        module {
            single<SqlDriver> { DatabaseDriverFactory(appContext).createDriver() }
        },
    )
}
