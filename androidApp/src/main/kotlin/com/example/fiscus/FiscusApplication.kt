package com.example.fiscus

import android.app.Application
import com.example.fiscus.di.initKoinAndroid

class FiscusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
