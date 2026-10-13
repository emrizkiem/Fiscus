package com.example.fiscus.di

import com.example.fiscus.data.local.createFiscusDatabase
import com.example.fiscus.data.ocr.OcrReceiptReader
import com.example.fiscus.data.ocr.ReceiptScanner
import com.example.fiscus.data.repository.ExpenseRepositoryImpl
import com.example.fiscus.domain.receipt.ReceiptReader
import com.example.fiscus.domain.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

val dataModule = module {
    single { ReceiptScanner() }
    single<ReceiptReader> { OcrReceiptReader(scanner = get()) }
    single { createFiscusDatabase(driver = get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(database = get(), ioDispatcher = Dispatchers.IO) }
}
