package com.example.fiscus.di

import app.cash.sqldelight.db.SqlDriver
import com.example.fiscus.data.local.createInMemoryDriver
import com.example.fiscus.data.repository.ExpenseRepositoryImpl
import com.example.fiscus.domain.repository.ExpenseRepository
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class DataModuleTest {
    private val testPlatformModule = module {
        single<SqlDriver> { createInMemoryDriver() }
    }

    @Test
    fun resolvesExpenseRepository() {
        val koin = koinApplication { modules(testPlatformModule, dataModule) }.koin

        assertIs<ExpenseRepositoryImpl>(koin.get<ExpenseRepository>())
    }

    @Test
    fun repositoryIsSingleton() {
        val koin = koinApplication { modules(testPlatformModule, dataModule) }.koin

        assertSame(koin.get<ExpenseRepository>(), koin.get<ExpenseRepository>())
    }
}
